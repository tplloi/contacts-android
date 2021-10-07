package contacts.core.entities.cursor

import android.database.Cursor
import contacts.core.EmailField
import contacts.core.Fields
import contacts.core.entities.Email

/**
 * Retrieves [Fields.Email] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class EmailCursor(cursor: Cursor) : AbstractDataCursor<EmailField>(cursor) {

    val type: Email.Type? by type(Fields.Email.Type, typeFromValue = Email.Type::fromValue)

    val label: String? by string(Fields.Email.Label)

    val address: String? by string(Fields.Email.Address)
}