package my.qrcode.scanner.feature.tabs.create.qr
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import my.qrcode.scanner.R
import my.qrcode.scanner.extension.isNotBlank
import my.qrcode.scanner.extension.textString
import my.qrcode.scanner.feature.tabs.create.BaseCreateBarcodeFragment
import my.qrcode.scanner.model.schema.Schema
import my.qrcode.scanner.model.schema.Sms
import kotlinx.android.synthetic.main.fragment_create_qr_code_sms.edit_text_phone
import kotlinx.android.synthetic.main.fragment_create_qr_code_sms.edit_text_message
class CreateQrCodeSmsFragment : BaseCreateBarcodeFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_qr_code_sms, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTitleEditText()
        handleTextChanged()
    }
    override fun showPhone(phone: String) {
        edit_text_phone.apply {
            setText(phone)
            setSelection(phone.length)
        }
    }
    override fun getBarcodeSchema(): Schema {
       return Sms(
           phone = edit_text_phone.textString,
           message = edit_text_message.textString
       )
    }
    private fun initTitleEditText() {
        edit_text_phone.requestFocus()
    }
    private fun handleTextChanged() {
        edit_text_phone.addTextChangedListener { toggleCreateBarcodeButton() }
        edit_text_message.addTextChangedListener { toggleCreateBarcodeButton() }
    }
    private fun toggleCreateBarcodeButton() {
        parentActivity.isCreateBarcodeButtonEnabled = edit_text_phone.isNotBlank() || edit_text_message.isNotBlank()
    }
}