package my.qrcode.scanner.feature.tabs.settings.theme
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import my.qrcode.scanner.R
import my.qrcode.scanner.di.settings
import my.qrcode.scanner.extension.applySystemWindowInsets
import my.qrcode.scanner.extension.unsafeLazy
import my.qrcode.scanner.feature.BaseActivity
import my.qrcode.scanner.usecase.Settings
import kotlinx.android.synthetic.main.activity_choose_theme.button_dark_theme
import kotlinx.android.synthetic.main.activity_choose_theme.button_light_theme
import kotlinx.android.synthetic.main.activity_choose_theme.button_system_theme
import kotlinx.android.synthetic.main.activity_choose_theme.toolbar
import kotlinx.android.synthetic.main.activity_choose_theme.root_view
class ChooseThemeActivity : BaseActivity() {
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ChooseThemeActivity::class.java)
            context.startActivity(intent)
        }
    }
    private val buttons by unsafeLazy {
        listOf(button_system_theme, button_light_theme, button_dark_theme)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_theme)
        supportEdgeToEdge()
        initToolbar()
    }
    override fun onResume() {
        super.onResume()
        showInitialSettings()
        handleSettingsChanged()
    }
    private fun supportEdgeToEdge() {
        root_view.applySystemWindowInsets(applyTop = true, applyBottom = true)
    }
    private fun initToolbar() {
        toolbar.setNavigationOnClickListener { finish() }
    }
    private fun showInitialSettings() {
        val theme = settings.theme
        button_system_theme.isChecked = theme == Settings.THEME_SYSTEM
        button_light_theme.isChecked = theme == Settings.THEME_LIGHT
        button_dark_theme.isChecked = theme == Settings.THEME_DARK
    }
    private fun handleSettingsChanged() {
        button_system_theme.setCheckedChangedListener { isChecked ->
            if (isChecked.not()) {
                return@setCheckedChangedListener
            }
            uncheckOtherButtons(button_system_theme)
            settings.theme = Settings.THEME_SYSTEM
        }
        button_light_theme.setCheckedChangedListener { isChecked ->
            if (isChecked.not()) {
                return@setCheckedChangedListener
            }
            uncheckOtherButtons(button_light_theme)
            settings.theme = Settings.THEME_LIGHT
        }
        button_dark_theme.setCheckedChangedListener { isChecked ->
            if (isChecked.not()) {
                return@setCheckedChangedListener
            }
            uncheckOtherButtons(button_dark_theme)
            settings.theme = Settings.THEME_DARK
        }
    }
    private fun uncheckOtherButtons(checkedButton: View) {
        buttons.forEach { button ->
            if (checkedButton !== button) {
                button.isChecked = false
            }
        }
    }
}