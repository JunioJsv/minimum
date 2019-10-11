package juniojsv.minimum

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import juniojsv.minimum.Settings.Companion.KEY_DARK_MODE
import kotlinx.android.synthetic.main.settings_activity.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Settings(this).apply {
            if (getBoolean(KEY_DARK_MODE)) {
                setTheme(R.style.AppThemeDark)
            }
        }
        super.onCreate(savedInstanceState)

        setContentView(R.layout.settings_activity).apply {
            title = "Settings"
            dark_mode.isChecked = Settings(this@SettingsActivity).getBoolean(KEY_DARK_MODE)
            version.text = "${getString(R.string.version)} ${BuildConfig.VERSION_NAME}"
        }

        dark_mode.setOnCheckedChangeListener { _, isChecked ->
            Settings(this).putBoolean(KEY_DARK_MODE, isChecked)
        }
    }

}
