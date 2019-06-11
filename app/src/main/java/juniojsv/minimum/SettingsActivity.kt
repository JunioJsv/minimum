package juniojsv.minimum

import android.os.Bundle
import android.preference.PreferenceActivity

class SettingsActivity : PreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        applySettings()
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.settings_activity)

        findPreference("dark_theme").sharedPreferences.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
            Settings(this).putBoolean(key, sharedPreferences.getBoolean(key, false))
        }
    }

    private fun applySettings() {
        Settings(this).apply {
            if (getBoolean("dark_theme")) {
                setTheme(R.style.AppThemeDark)
            }
        }

    }

}
