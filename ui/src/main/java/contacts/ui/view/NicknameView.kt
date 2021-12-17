package contacts.ui.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import contacts.core.entities.NicknameEntity
import contacts.ui.R

/**
 * A [DataEntityView] for a [NicknameEntity].
 */
class NicknameView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : DataEntityView<NicknameEntity>(
    context, attributeSet, defStyleAttr,
    dataFieldInputType = InputType.TYPE_TEXT_VARIATION_PERSON_NAME,
    dataFieldHintResId = R.string.contacts_ui_nickname_hint,
    dataDeleteButtonIsVisible = false
)