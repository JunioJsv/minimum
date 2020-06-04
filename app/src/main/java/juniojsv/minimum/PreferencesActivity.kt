package juniojsv.minimum

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

class PreferencesActivity : AppCompatActivity() {
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        appearanceHandler(preferences)
        setContentView(R.layout.preferences_activity)
        registerActivity(this)
        supportFragmentManager.commit {
            replace(R.id.mPreference_fragment, PreferencesFragment())
        }
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
            findPreference<Preference>("application_version")?.summary =
                    "${getString(R.string.version)} ${BuildConfig.VERSION_NAME}"
        }
    }

    companion object {
        private val activities = mutableListOf<AppCompatActivity>()

        private object PreferencesHandler : SharedPreferences.OnSharedPreferenceChangeListener {
            override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
                when (key) {
                    "dark_mode", "accent_color" -> {
                        activities.forEach { activity -> activity.recreate() }
                    }
                }
            }
        }

        fun registerActivity(activity: AppCompatActivity) {
            activities.find { registered -> activity.localClassName == registered.localClassName }?.let { target ->
                activities[activities.indexOf(target)] = activity
            } ?: activities.add(activity)
        }
    }
}