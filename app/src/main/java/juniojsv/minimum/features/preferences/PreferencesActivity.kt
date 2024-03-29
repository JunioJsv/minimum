package juniojsv.minimum.features.preferences

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.preference.PreferenceManager
import juniojsv.minimum.R
import juniojsv.minimum.databinding.PreferencesActivityBinding
import juniojsv.minimum.setActivityThemeByPreferences

class PreferencesActivity : AppCompatActivity(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var binding: PreferencesActivityBinding
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        preferences.registerOnSharedPreferenceChangeListener(this)
        setActivityThemeByPreferences(preferences)

        binding = PreferencesActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        with(supportFragmentManager) {
            if (findFragmentByTag(PreferencesFragment.TAG) == null) {
                commit {
                    add(
                        R.id.preferences_fragment,
                        PreferencesFragment(),
                        PreferencesFragment.TAG
                    )
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        preferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            getString(R.string.pref_activate_dark_mode_key),
            getString(R.string.pref_theme_accent_color_key) -> {
                recreate()
            }
        }
    }
}