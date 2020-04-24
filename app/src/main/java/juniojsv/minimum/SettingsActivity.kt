package juniojsv.minimum

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import juniojsv.minimum.SettingsManager.Companion.KEY_DARK_MODE
import juniojsv.minimum.SettingsManager.Companion.KEY_FAST_SCROLL
import kotlinx.android.synthetic.main.settings_activity.*

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        SettingsManager(this).also { settings ->
            if (settings.getBoolean(KEY_DARK_MODE)) setTheme(R.style.dark_mode)
            super.onCreate(savedInstanceState)

            setContentView(R.layout.settings_activity).apply {
                title = "Settings"
                dark_mode.isChecked = settings.getBoolean(KEY_DARK_MODE)
                fast_scroll.isChecked = settings.getBoolean(KEY_FAST_SCROLL)
                version.text = String.format("%s %s", getString(R.string.version), BuildConfig.VERSION_NAME)
            }

            dark_mode.setOnCheckedChangeListener { _, isChecked ->
                settings.putBoolean(KEY_DARK_MODE, isChecked)
            }

            fast_scroll.setOnCheckedChangeListener { _, isChecked ->
                settings.putBoolean(KEY_FAST_SCROLL, isChecked)
            }
        }
    }
}
