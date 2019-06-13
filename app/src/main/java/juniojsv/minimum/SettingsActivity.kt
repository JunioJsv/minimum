package juniojsv.minimum

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.settings_activity.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        applySettings()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity).apply {
            title = "Settings"
            dark_mode.isChecked = Settings(this@SettingsActivity).getBoolean("dark_theme")
            version.text = "${getString(R.string.version)} ${BuildConfig.VERSION_NAME}"
        }

        dark_mode.setOnCheckedChangeListener { _, isChecked ->
            Settings(this).putBoolean("dark_theme", isChecked)
        }

        thisActivity = this
    }

    private fun applySettings() {
        Settings(this).apply {
            if (getBoolean("dark_theme")) {
                setTheme(R.style.AppThemeDark)
            }
        }

    }

    companion object {
        private var thisActivity: SettingsActivity? = null
        fun recreate() {
            thisActivity?.apply {
                recreate()
            }
        }
    }

}
