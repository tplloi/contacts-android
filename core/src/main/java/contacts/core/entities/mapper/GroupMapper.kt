package contacts.core.entities.mapper

import contacts.core.entities.Group
import contacts.core.entities.cursor.GroupsCursor
import contacts.core.entities.cursor.account

internal class GroupMapper(private val groupsCursor: GroupsCursor) : EntityMapper<Group> {

    override val value: Group
        get() = Group(
            id = groupsCursor.id,
            systemId = groupsCursor.systemId,

            title = groupsCursor.title,

            isReadOnly = groupsCursor.isReadOnly,
            favorites = groupsCursor.favorites,
            autoAdd = groupsCursor.autoAdd,

            account = groupsCursor.account(),
            sourceId = groupsCursor.sourceId,

            isRedacted = false
        )
}
