package contacts.entities

import android.provider.ContactsContract.CommonDataKinds
import contacts.entities.Im.Protocol
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class Im internal constructor(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override val isPrimary: Boolean,

    override val isSuperPrimary: Boolean,

    // Type and Label are also available. However, they have no use here as the protocol and custom
    // protocol have taken their place...

    /**
     * The [Protocol] of this Im.
     */
    val protocol: Protocol?,

    /**
     * The name of the custom protocol. Used when the [protocol] is [Protocol.CUSTOM].
     */
    val customProtocol: String?,

    /**
     * The data as the user entered it.
     */
    val data: String?

) : CommonDataEntity {

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.Im

    // protocol and customProtocol are excluded from this check as they are useless information by
    // themselves
    @IgnoredOnParcel
    override val isBlank: Boolean = propertiesAreAllNullOrBlank(data)

    fun toMutableIm() = MutableIm(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        protocol = protocol,
        customProtocol = customProtocol,

        data = data
    )

    enum class Protocol(override val value: Int) : CommonDataEntity.Type {

        // Order of declaration is the same as seen in the native contacts app
        AIM(CommonDataKinds.Im.PROTOCOL_AIM), // Default
        MSN(CommonDataKinds.Im.PROTOCOL_MSN),
        YAHOO(CommonDataKinds.Im.PROTOCOL_YAHOO),
        SKYPE(CommonDataKinds.Im.PROTOCOL_SKYPE),
        QQ(CommonDataKinds.Im.PROTOCOL_QQ),
        HANGOUTS(CommonDataKinds.Im.PROTOCOL_GOOGLE_TALK),
        ICQ(CommonDataKinds.Im.PROTOCOL_ICQ),
        JABBER(CommonDataKinds.Im.PROTOCOL_JABBER),
        NET_MEETING(CommonDataKinds.Im.PROTOCOL_NETMEETING),
        CUSTOM(CommonDataKinds.Im.PROTOCOL_CUSTOM);

        internal companion object {

            fun fromValue(value: Int?): Protocol? = values().find { it.value == value }
        }
    }
}

@Parcelize
data class MutableIm internal constructor(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override var isPrimary: Boolean,

    override var isSuperPrimary: Boolean,

    /**
     * See [Im.protocol].
     */
    var protocol: Protocol?,

    /**
     * See [Im.customProtocol].
     */
    var customProtocol: String?,

    /**
     * See [Im.data].
     */
    var data: String?

) : MutableCommonDataEntity {

    constructor() : this(
        null, null, null, false, false,
        null, null, null
    )

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.Im

    // protocol and customProtocol are excluded from this check as they are useless information by
    // themselves
    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(data)
}