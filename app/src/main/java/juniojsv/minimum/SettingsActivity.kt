package juniojsv.minimum

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceActivity

class SettingsActivity : PreferenceActivity() {
    private lateinit var checkBoxDarkTheme: SharedPreferences
    private val settingEditor: SharedPreferences.Editor = MinimumActivity.settings.edit()
    private val DARK_THEME_PREF: Boolean = MinimumActivity.settings.getBoolean("dark_theme", false)

    override fun onCreate(savedInstanceState: Bundle?) {
        applySettings()
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.settings_activity)
        checkBoxDarkTheme = findPreference("prefs_dark_theme").sharedPreferences
    }

    override fun onPause() {
        super.onPause()
        updateSettings()
    }

    private fun updateSettings() {
        if (checkBoxDarkTheme.getBoolean("prefs_dark_theme", false) != DARK_THEME_PREF) {
            settingEditor.putBoolean("dark_theme", checkBoxDarkTheme.getBoolean("prefs_dark_theme", false))
            needRestart = true
        }

        if (needRestart) {
            settingEditor.apply()
            recreate()
        }
    }

    private fun applySettings() {
        if (MinimumActivity.settings.getBoolean("dark_theme", false)) {
            setTheme(R.style.AppThemeDark)
        }
    }

    companion object {
        var needRestart: Boolean = false
    }
}
