package contacts.sample.view

import android.accounts.Account
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.widget.TextView
import contacts.core.Contacts
import contacts.core.entities.NewRawContact
import contacts.core.entities.RawContactEntity
import contacts.sample.AccountsActivity
import contacts.ui.view.activity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

/**
 * A [TextView] that displays an immutable [Account] from the given [RawContactEntity].
 *
 * Setting the [rawContact] will automatically update the views.
 *
 * ## Note
 *
 * This is a very rudimentary view that is not styled or made to look good. It may not follow any
 * good practices and may even implement bad practices. Consumers of the library may choose to use
 * this as is or simply as a reference on how to implement this part of native Contacts app.
 *
 * This does not support state retention (e.g. device rotation). The OSS community may contribute to
 * this by implementing it.
 *
 * The community may contribute by styling and adding more features and customizations with these
 * views if desired.
 *
 * ## Developer Notes
 *
 * I usually am a proponent of passive views and don't add any logic to views. However, I will make
 * an exception for this basic view that I don't really encourage consumers to use.
 *
 * This is in the sample and not in the contacts-ui module because it requires concurrency. We
 * should not add coroutines and contacts-async as dependencies to contacts-ui just for this.
 * Consumers may copy and paste this into their projects or if the community really wants it, we may
 * move this to a separate module (contacts-ui-async).
 */
class AccountView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextView(context, attributeSet, defStyleAttr), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + Dispatchers.Main

    private var rawContact: RawContactEntity? = null

    var account: Account? = null
        private set

    /**
     * Sets the RawContact account shown and managed by this view to the given [rawContact] and uses
     * the given [contacts] API to perform operations on it. The [defaultAccount] is used if the
     * [rawContact] is not yet associated with an account.
     */
    fun setRawContact(
        contacts: Contacts,
        rawContact: RawContactEntity?,
        defaultAccount: Account?
    ) {
        this.rawContact = rawContact
        setAccount(rawContact?.accountOrNull)
    }

    private fun setAccount(account: Account?) {
        this.account = account

        text = if (account == null) {
            """
                Local account (device only)
                Not synced
            """
        } else {
            """
                Account Name: ${account.name}
                Account Type: ${account.type}
            """
        }.trimIndent()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setOnClickListener {
            activity?.let {
                AccountsActivity.selectAccounts(it, false, arrayListOf(account))
            }
        }
    }

    /**
     * Invoke this method on the host activity's onActivityResult in order to process the picked
     * account (if any). This will do nothing if the request did not originate from this view.
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        AccountsActivity.onSelectAccountsResult(requestCode, resultCode, data) {
            setAccount(it.firstOrNull())
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(
            if (rawContact is NewRawContact) {
                enabled
            } else {
                // Never enable if not a NewRawContact.
                false
            }
        )
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancel()
    }
}