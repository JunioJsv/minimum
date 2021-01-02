package juniojsv.minimum

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import juniojsv.minimum.databinding.PreferencesActivityBinding

class PreferencesActivity : AppCompatActivity(), PreferencesHandler.OnPreferenceChangeListener {
    private lateinit var binding: PreferencesActivityBinding
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        appearanceHandler(preferences)

        binding = PreferencesActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.commit {
            replace(R.id.mPreference_fragment, PreferencesFragment())
        }

        PreferencesHandler.addListener(this)
    }

    override fun onResume() {
        super.onResume()
        preferences.registerOnSharedPreferenceChangeListener(PreferencesHandler)
    }

    override fun onPause() {
        super.onPause()
        preferences.unregisterOnSharedPreferenceChangeListener(PreferencesHandler)
    }

    class PreferencesFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)
            findPreference<Preference>(APPLICATION_VERSION)?.summary =
                    "${getString(R.string.version)} ${BuildConfig.VERSION_NAME}"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PreferencesHandler.removeListener(this)
    }

    override fun onPreferenceChange(key: String) {
        when(key) {
            DARK_MODE, ACCENT_COLOR -> {
                recreate()
            }
        }
    }

    companion object Keys {
        const val GRID_VIEW = "grid_view"
        const val GRID_VIEW_COLUMNS = "grid_view_columns"
        const val DARK_MODE = "dark_mode"
        const val ACCENT_COLOR = "accent_color"
        const val ACCENT_COLOR_RED = "red"
        const val ACCENT_COLOR_GREEN = "green"
        const val ACCENT_COLOR_BLUE = "blue"
        const val APPLICATION_VERSION = "application_version"
    }
}