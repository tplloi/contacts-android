package com.vestrel00.contacts.groups

import android.accounts.Account
import android.content.Context
import android.provider.ContactsContract
import com.vestrel00.contacts.ContactsPermissions
import com.vestrel00.contacts.accounts.Accounts
import com.vestrel00.contacts.entities.MutableGroup
import com.vestrel00.contacts.entities.operation.GroupOperation

/**
 * Inserts one or more user groups into the groups table.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.WRITE_PERMISSION] and
 * [com.vestrel00.contacts.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION] are assumed to have
 * been granted already in these  examples for brevity. All inserts will do nothing if these
 * permissions are not granted.
 *
 * ## Accounts
 *
 * The get accounts permission is required here because this API retrieves all available accounts,
 * if any, and does the following;
 *
 * - if the account specified is found in the list of accounts returned by the system, the account
 * is used
 * - if the account specified is not found in the list of accounts returned by the system, the first
 * account returned by the system is used
 *
 * In other words, this API does not allow groups to not have an associated account if account(s)
 * are available.
 *
 * Actually, the Contacts Provider automatically updates the groups to be associated with an
 * existing account (if available). The Contacts Provider does not allow null accounts associated
 * with groups when there are existing accounts. The Contacts Provider enforces this rule by
 * routinely checking for groups associated with null accounts and assigns non-null accounts to
 * those groups.
 *
 * This API takes the initiative instead of waiting for the Contacts Provider to assign accounts
 * (and groups) at some later point in time. This ensures that consumers are not subject to the
 * "randomness"/asynchronous nature of the Contacts Provider.
 *
 * ## Usage
 *
 * To insert a group with the title "Best Friends" for the given account;
 *
 * In Kotlin,
 *
 * ```kotlin
 * val result = groupsInsert
 *      .groups(MutableGroup("Best Friends", account))
 *      .commit()
 * ```
 *
 * In Java,
 *
 * ```java
 * GroupsInsert.Result result = groupsInsert
 *      .groups(new MutableGroup("Best Friends", account))
 *      .commit();
 * ```
 */
interface GroupsInsert {

    /**
     * Adds the given [groups] to the insert queue, which will be inserted on [commit].
     * Duplicates (groups with identical attributes to already added groups) are ignored.
     */
    fun groups(vararg groups: MutableGroup): GroupsInsert

    /**
     * See [GroupsInsert.groups].
     */
    fun groups(groups: Collection<MutableGroup>): GroupsInsert

    /**
     * See [GroupsInsert.groups].
     */
    fun groups(groups: Sequence<MutableGroup>): GroupsInsert

    /**
     * Inserts the [MutableGroup]s in the queue (added via [groups]) and returns the [Result].
     *
     * Groups with titles that already exist will be inserted. The Contacts Provider allows this
     * and is the behavior of the native Contacts app. If desired, it is up to consumers to protect
     * against multiple groups from the same account having the same titles.
     *
     * This does nothing if there are no available accounts or no groups are in the insert queue or
     * if insert permission has not been granted. An empty map will be returned in this case.
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun commit(): Result

    interface Result {

        /**
         * The list of IDs of successfully created Groups.
         */
        val groupIds: List<Long>

        /**
         * True if all MutableGroups have successfully been inserted. False if even one insert
         * failed.
         */
        val isSuccessful: Boolean

        /**
         * True if the [group] has been successfully inserted. False otherwise.
         */
        fun isSuccessful(group: MutableGroup): Boolean

        /**
         * Returns the ID of the newly created Group. Use the ID to get the newly created Group via
         * a query. The manually constructed [MutableGroup] passed to [GroupsInsert.groups] are not
         * automatically updated and will remain to have an invalid ID.
         *
         * Returns null if the insert operation failed.
         */
        fun groupId(group: MutableGroup): Long?
    }
}

@Suppress("FunctionName")
internal fun GroupsInsert(context: Context): GroupsInsert = GroupsInsertImpl(
    context,
    Accounts(),
    ContactsPermissions(context)
)

private class GroupsInsertImpl(
    private val context: Context,
    private val accounts: Accounts,
    private val permissions: ContactsPermissions,
    private val groups: MutableSet<MutableGroup> = mutableSetOf()
) : GroupsInsert {

    override fun groups(vararg groups: MutableGroup): GroupsInsert =
        groups(groups.asSequence())

    override fun groups(groups: Collection<MutableGroup>): GroupsInsert =
        groups(groups.asSequence())

    override fun groups(groups: Sequence<MutableGroup>): GroupsInsert = apply {
        this.groups.addAll(groups)
    }

    override fun commit(): GroupsInsert.Result {
        val accounts = accounts.allAccounts(context)
        if (accounts.isEmpty() || groups.isEmpty() || !permissions.canInsertUpdateDelete()) {
            return GroupsInsertFailed
        }

        val results = mutableMapOf<MutableGroup, Long?>()
        for (group in groups) {
            results[group] = insertGroup(group.withValidAccount(accounts))
        }
        return GroupsInsertResult(results)
    }

    private fun insertGroup(group: MutableGroup): Long? {
        val operation = GroupOperation().insert(group)

        /*
         * Atomically insert the group row.
         *
         * Perform this single operation in a batch to be consistent with the other CRUD functions.
         */
        val results = try {
            context.contentResolver.applyBatch(ContactsContract.AUTHORITY, arrayListOf(operation))
        } catch (exception: Exception) {
            null
        }

        /*
         * The ContentProviderResult[0] contains the first result of the batch, which is the
         * GroupOperation. The uri contains the Groups._ID as the last path segment.
         *
         * E.G. "content://com.android.contacts/groups/18"
         * In this case, 18 is the Groups._ID.
         *
         * It is formed by the Contacts Provider using
         * Uri.withAppendedPath(ContactsContract.Groups.CONTENT_URI, "18")
         */
        return results?.firstOrNull()?.let { result ->
            val groupUri = result.uri
            val groupId = groupUri.lastPathSegment?.toLongOrNull()
            groupId
        }
    }

    private fun MutableGroup.withValidAccount(accounts: List<Account>): MutableGroup {
        if (!accounts.contains(account)) {
            // We dissuade consumers from using the copy method. However, we know what we are doing
            // so we make an exception here =)
            return this.copy(account = accounts.first())
        }

        return this
    }
}

private class GroupsInsertResult(private val groupsMap: Map<MutableGroup, Long?>) :
    GroupsInsert.Result {

    override val groupIds: List<Long> by lazy {
        groupsMap.asSequence()
            .filter { it.value != null }
            .map { it.value!! }
            .toList()
    }

    override val isSuccessful: Boolean by lazy { groupsMap.all { it.value != null } }

    override fun isSuccessful(group: MutableGroup): Boolean = groupId(group) != null

    override fun groupId(group: MutableGroup): Long? = groupsMap.getOrElse(group) { null }
}

private object GroupsInsertFailed : GroupsInsert.Result {

    override val groupIds: List<Long> = emptyList()

    override val isSuccessful: Boolean = false

    override fun isSuccessful(group: MutableGroup): Boolean = false

    override fun groupId(group: MutableGroup): Long? = null
}