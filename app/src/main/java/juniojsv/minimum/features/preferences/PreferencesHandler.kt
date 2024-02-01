package juniojsv.minimum.features.preferences

import android.content.SharedPreferences

object PreferencesHandler : SharedPreferences.OnSharedPreferenceChangeListener {
    interface OnPreferenceChangeListener {
        fun onPreferenceChange(key: String)
    }

    private val listeners = arrayListOf<OnPreferenceChangeListener>()

    fun addListener(listener: OnPreferenceChangeListener) = listeners.add(listener)

    fun removeListener(listener: OnPreferenceChangeListener) = listeners.remove(listener)

    fun removeAllListeners() = listeners.clear()

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) =
        listeners.forEach {
            it.onPreferenceChange(key)
        }
}