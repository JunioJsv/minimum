package juniojsv.minimum

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import juniojsv.minimum.SettingsManager.Companion.KEY_DARK_MODE
import kotlinx.android.synthetic.main.settings_activity.*

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        SettingsManager(this).also { settings ->
            if (settings.getBoolean(KEY_DARK_MODE)) setTheme(R.style.dark_mode)
            super.onCreate(savedInstanceState)

            setContentView(R.layout.settings_activity).apply {
                title = getString(R.string.settings_shortcut)
                dark_mode.isChecked = settings.getBoolean(KEY_DARK_MODE)
                version.text = String.format("%s %s", getString(R.string.version_title), BuildConfig.VERSION_NAME)
            }

            dark_mode.setOnCheckedChangeListener { _, isChecked ->
                settings.putBoolean(KEY_DARK_MODE, isChecked)
            }
        }
    }
}
