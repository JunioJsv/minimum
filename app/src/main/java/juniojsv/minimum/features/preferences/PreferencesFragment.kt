package juniojsv.minimum.features.preferences

import android.os.Build
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import juniojsv.minimum.BuildConfig
import juniojsv.minimum.R

class PreferencesFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                R.string.pref_activate_dark_mode_key,
                R.string.pref_theme_accent_color_key
            ).forEach { id ->
                findPreference<Preference>(getString(id))?.isVisible = false
            }
        }
        findPreference<Preference>(getString(R.string.pref_application_version_key))?.summary =
            "${getString(R.string.version)} ${BuildConfig.VERSION_NAME}"
    }
}