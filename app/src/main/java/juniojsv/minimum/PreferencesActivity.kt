package juniojsv.minimum

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

class PreferencesActivity : AppCompatActivity(), PreferencesEventHandler.Listener {
    private lateinit var preferences: SharedPreferences
    private val preferencesHandler = PreferencesHandler(this)
    private val preferencesEventHandler = PreferencesEventHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        appearanceHandler(preferences)
        setContentView(R.layout.preferences_activity)
        supportFragmentManager.commit {
            replace(R.id.mPreference_fragment, PreferencesFragment())
        }
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(preferencesEventHandler, PreferencesEventHandler.DEFAULT_INTENT_FILTER)
    }

    override fun onResume() {
        super.onResume()
        preferences.registerOnSharedPreferenceChangeListener(preferencesHandler)
    }

    override fun onPause() {
        super.onPause()
        preferences.unregisterOnSharedPreferenceChangeListener(preferencesHandler)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(preferencesEventHandler)
    }

    class PreferencesFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)
            findPreference<Preference>("application_version")?.summary =
                    "${getString(R.string.version)} ${BuildConfig.VERSION_NAME}"
        }
    }

    override fun onPreferenceEvent(intent: Intent) {
        when (intent.action) {
            PreferencesEventHandler.ACTION_FORCE_RECREATE -> {
                val value = intent.getStringExtra("activity")
                if (value == "all" || value == "preferences")
                    recreate()
            }
        }
    }
}