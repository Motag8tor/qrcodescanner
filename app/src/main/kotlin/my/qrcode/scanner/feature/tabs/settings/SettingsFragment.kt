package my.qrcode.scanner.feature.tabs.settings
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import my.qrcode.scanner.BuildConfig
import my.qrcode.scanner.R
import my.qrcode.scanner.di.barcodeDatabase
import my.qrcode.scanner.di.settings
import my.qrcode.scanner.extension.applySystemWindowInsets
import my.qrcode.scanner.extension.packageManager
import my.qrcode.scanner.extension.showError
import my.qrcode.scanner.feature.common.dialog.DeleteConfirmationDialogFragment
import my.qrcode.scanner.feature.tabs.settings.camera.ChooseCameraActivity
import my.qrcode.scanner.feature.tabs.settings.formats.SupportedFormatsActivity
import my.qrcode.scanner.feature.tabs.settings.more.MoreFragment
import my.qrcode.scanner.feature.tabs.settings.permissions.AllPermissionsActivity
import my.qrcode.scanner.feature.tabs.settings.search.ChooseSearchEngineActivity
import my.qrcode.scanner.feature.tabs.settings.theme.ChooseThemeActivity
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_settings.app_bar_layout
import kotlinx.android.synthetic.main.fragment_settings.button_app_version
import kotlinx.android.synthetic.main.fragment_settings.button_check_updates
import kotlinx.android.synthetic.main.fragment_settings.button_choose_camera
import kotlinx.android.synthetic.main.fragment_settings.button_choose_search_engine
import kotlinx.android.synthetic.main.fragment_settings.button_choose_theme
import kotlinx.android.synthetic.main.fragment_settings.button_clear_history
import kotlinx.android.synthetic.main.fragment_settings.button_confirm_scans_manually
import kotlinx.android.synthetic.main.fragment_settings.button_continuous_scanning
import kotlinx.android.synthetic.main.fragment_settings.button_copy_to_clipboard
import kotlinx.android.synthetic.main.fragment_settings.button_do_not_save_duplicates
import kotlinx.android.synthetic.main.fragment_settings.button_flashlight
import kotlinx.android.synthetic.main.fragment_settings.button_inverse_barcode_colors_in_dark_theme
import kotlinx.android.synthetic.main.fragment_settings.button_open_links_automatically
import kotlinx.android.synthetic.main.fragment_settings.button_permissions
import kotlinx.android.synthetic.main.fragment_settings.button_simple_auto_focus
import kotlinx.android.synthetic.main.fragment_settings.button_save_scanned_barcodes
import kotlinx.android.synthetic.main.fragment_settings.button_vibrate
import kotlinx.android.synthetic.main.fragment_settings.button_select_supported_formats
import kotlinx.android.synthetic.main.fragment_settings.button_source_code
import kotlinx.android.synthetic.main.fragment_settings.button_changelog
import kotlinx.android.synthetic.main.fragment_settings.button_save_created_barcodes
import kotlinx.android.synthetic.main.fragment_settings.button_oss_libraries
import kotlinx.android.synthetic.main.fragment_settings.button_more
class SettingsFragment : Fragment(), DeleteConfirmationDialogFragment.Listener {
    private val disposable = CompositeDisposable()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        supportEdgeToEdge()
    }
    override fun onResume() {
        super.onResume()
        handleButtonCheckedChanged()
        handleButtonClicks()
        showSettings()
        showAppVersion()
    }
    override fun onDeleteConfirmed() {
        clearHistory()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        disposable.clear()
    }
    fun supportEdgeToEdge() {
        app_bar_layout.applySystemWindowInsets(applyTop = true)
    }
    private fun handleButtonCheckedChanged() {
        button_inverse_barcode_colors_in_dark_theme.setCheckedChangedListener { settings.areBarcodeColorsInversed = it }
        button_open_links_automatically.setCheckedChangedListener { settings.openLinksAutomatically = it }
        button_copy_to_clipboard.setCheckedChangedListener { settings.copyToClipboard = it }
        button_simple_auto_focus.setCheckedChangedListener { settings.simpleAutoFocus = it }
        button_flashlight.setCheckedChangedListener { settings.flash = it }
        button_vibrate.setCheckedChangedListener { settings.vibrate = it }
        button_continuous_scanning.setCheckedChangedListener { settings.continuousScanning = it }
        button_confirm_scans_manually.setCheckedChangedListener { settings.confirmScansManually = it }
        button_save_scanned_barcodes.setCheckedChangedListener { settings.saveScannedBarcodesToHistory = it }
        button_save_created_barcodes.setCheckedChangedListener { settings.saveCreatedBarcodesToHistory = it }
        button_do_not_save_duplicates.setCheckedChangedListener { settings.doNotSaveDuplicates = it }
    }
    private fun handleButtonClicks() {
        button_choose_theme.setOnClickListener { ChooseThemeActivity.start(requireActivity()) }
        button_choose_camera.setOnClickListener { ChooseCameraActivity.start(requireActivity()) }
        button_select_supported_formats.setOnClickListener { SupportedFormatsActivity.start(requireActivity()) }
        button_clear_history.setOnClickListener { showDeleteHistoryConfirmationDialog() }
        button_choose_search_engine.setOnClickListener { ChooseSearchEngineActivity.start(requireContext()) }
        button_permissions.setOnClickListener { AllPermissionsActivity.start(requireActivity()) }
        button_oss_libraries.setOnClickListener { ossLicensesActivity() }
        button_check_updates.setOnClickListener { showAppInMarket() }
        button_source_code.setOnClickListener { showSourceCode() }
        button_changelog.setOnClickListener { showChangelog() }
        button_more.setOnClickListener { showMore() }
    }
    private fun clearHistory() {
        button_clear_history.isEnabled = false
        barcodeDatabase.deleteAll()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    button_clear_history.isEnabled = true
                },
                { error ->
                    button_clear_history.isEnabled = true
                    showError(error)
                }
            )
            .addTo(disposable)
    }
    private fun showSettings() {
        settings.apply {
            button_inverse_barcode_colors_in_dark_theme.isChecked = areBarcodeColorsInversed
            button_open_links_automatically.isChecked = openLinksAutomatically
            button_copy_to_clipboard.isChecked = copyToClipboard
            button_simple_auto_focus.isChecked = simpleAutoFocus
            button_flashlight.isChecked = flash
            button_vibrate.isChecked = vibrate
            button_continuous_scanning.isChecked = continuousScanning
            button_confirm_scans_manually.isChecked = confirmScansManually
            button_save_scanned_barcodes.isChecked = saveScannedBarcodesToHistory
            button_save_created_barcodes.isChecked = saveCreatedBarcodesToHistory
            button_do_not_save_duplicates.isChecked = doNotSaveDuplicates
        }
    }
    private fun showDeleteHistoryConfirmationDialog() {
        val dialog = DeleteConfirmationDialogFragment.newInstance(R.string.dialog_delete_clear_history_message)
        dialog.show(childFragmentManager, "")
    }
    private fun ossLicensesActivity() {
        OssLicensesMenuActivity.setActivityTitle(getString(R.string.fragment_settings_license_title))
        val intent = Intent(activity, OssLicensesMenuActivity::class.java)
        startActivity(intent)
    }
    private fun showMore() {
        val intent = Intent(activity, MoreFragment::class.java)
        startActivity(intent)
    }
    private fun showAppInMarket() {
        val uri = Uri.parse("market://details?id=" + requireContext().packageName)
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        }
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        }
    }
    private fun showSourceCode() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://bit.ly/qrcodescannergithub"))
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }
    private fun showChangelog() {
        val alertDialog = AlertDialog.Builder(requireActivity())
        alertDialog.setTitle(R.string.fragment_settings_changelog)
        alertDialog.setMessage(R.string.changelog)
        alertDialog.setPositiveButton("Cool!") { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        alertDialog.show()
    }
    private fun showAppVersion() {
        button_app_version.hint = BuildConfig.VERSION_NAME
    }
}