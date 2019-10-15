package juniojsv.minimum

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import juniojsv.minimum.Settings.Companion.KEY_DARK_MODE
import kotlinx.android.synthetic.main.settings_activity.*

class SettingsActivity : AppCompatActivity() {
    private lateinit var settings : Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        Settings(this).also { settings = it }
        settings.apply {
            if (getBoolean(KEY_DARK_MODE)) {
                setTheme(R.style.AppThemeDark)
            }
        }
        super.onCreate(savedInstanceState)

        setContentView(R.layout.settings_activity).apply {
            title = "Settings"
            dark_mode.isChecked = settings.getBoolean(KEY_DARK_MODE)
            version.text = "${getString(R.string.version)} ${BuildConfig.VERSION_NAME}"
        }

        dark_mode.setOnCheckedChangeListener { _, isChecked ->
            settings.putBoolean(KEY_DARK_MODE, isChecked)
        }
    }

}
