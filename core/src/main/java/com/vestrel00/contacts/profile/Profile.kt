package com.vestrel00.contacts.profile

import android.content.Context
import com.vestrel00.contacts.ContactsPermissions

/**
 * Provides new [ProfileQuery], [ProfileInsert], [ProfileUpdate], and [ProfileDelete] instances.
 *
 * ## Permissions
 *
 * - Add the "android.permission.READ_CONTACTS" to the AndroidManifest in order to [query].
 * - Add the "android.permission.WRITE_CONTACTS" to the AndroidManifest in order to [insert],
 * [update], and [delete].
 *
 * Use [permissions] convenience functions to check for required permissions.
 */
interface Profile {

    /**
     * Returns a new [ProfileQuery] instance.
     */
    fun query(): ProfileQuery

    /**
     * Returns a new [ProfileInsert] instance.
     */
    fun insert(): ProfileInsert

    /**
     * Returns a new [ProfileUpdate] instance.
     */
    fun update(): ProfileUpdate

    /**
     * Returns a new [ProfileDelete] instance.
     */
    fun delete(): ProfileDelete

    /**
     * Returns a new [ContactsPermissions] instance, which provides functions for checking required
     * permissions.
     */
    fun permissions(): ContactsPermissions

    /**
     * Reference to the Application's Context for use in extension functions and external library
     * modules. This is safe to hold on to. Not meant for consumer use.
     */
    val applicationContext: Context
}

@Suppress("FunctionName")
internal fun Profile(context: Context): Profile = ProfileImpl(context.applicationContext)

private class ProfileImpl(override val applicationContext: Context) : Profile {

    override fun query() = ProfileQuery(applicationContext)

    override fun insert() = ProfileInsert(applicationContext)

    override fun update() = ProfileUpdate(applicationContext)

    override fun delete() = ProfileDelete(applicationContext)

    override fun permissions() = ContactsPermissions(applicationContext)
}