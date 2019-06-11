package juniojsv.minimum

import android.content.Context

class Settings(context: Context) {

    private val settings = context.getSharedPreferences("${context.packageName}_settings", Context.MODE_PRIVATE).apply {
        registerOnSharedPreferenceChangeListener { _, key ->
            if (key == "dark_theme") {
                MinimumActivity.recreate()
                SettingsActivity.recreate()
            }
        }
    }

    fun putBoolean(key: String, boolean: Boolean) {
        settings.edit().apply {
            putBoolean(key, boolean).commit()
        }.apply()
    }

    fun getBoolean(key: String): Boolean {
        return settings.getBoolean(key, false)
    }

}