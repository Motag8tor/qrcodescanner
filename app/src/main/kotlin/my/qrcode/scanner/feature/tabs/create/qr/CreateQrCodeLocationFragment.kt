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
import my.qrcode.scanner.model.schema.Geo
import my.qrcode.scanner.model.schema.Schema
import kotlinx.android.synthetic.main.fragment_create_qr_code_location.edit_text_longitude
import kotlinx.android.synthetic.main.fragment_create_qr_code_location.edit_text_latitude
import kotlinx.android.synthetic.main.fragment_create_qr_code_location.edit_text_altitude
class CreateQrCodeLocationFragment : BaseCreateBarcodeFragment() {
    override val latitude: Double?
        get() = edit_text_latitude.textString.toDoubleOrNull()
    override val longitude: Double?
        get() = edit_text_longitude.textString.toDoubleOrNull()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_qr_code_location, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLatitudeEditText()
        handleTextChanged()
    }
    override fun getBarcodeSchema(): Schema {
       return Geo(
           latitude = edit_text_latitude.textString,
           longitude = edit_text_longitude.textString,
           altitude = edit_text_altitude.textString
       )
    }
    override fun showLocation(latitude: Double?, longitude: Double?) {
        latitude?.apply {
            edit_text_latitude.setText(latitude.toString())
        }
        longitude?.apply {
            edit_text_longitude.setText(longitude.toString())
        }
    }
    private fun initLatitudeEditText() {
        edit_text_latitude.requestFocus()
    }
    private fun handleTextChanged() {
        edit_text_latitude.addTextChangedListener { toggleCreateBarcodeButton() }
        edit_text_longitude.addTextChangedListener { toggleCreateBarcodeButton() }
    }
    private fun toggleCreateBarcodeButton() {
        parentActivity.isCreateBarcodeButtonEnabled = edit_text_latitude.isNotBlank() && edit_text_longitude.isNotBlank()
    }
}