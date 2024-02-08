package juniojsv.minimum.features.preferences

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import juniojsv.minimum.BuildConfig
import juniojsv.minimum.R

class PreferencesFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        findPreference<Preference>(getString(R.string.pref_application_version_key))?.summary =
            "${getString(R.string.version)} ${BuildConfig.VERSION_NAME}"
    }
}