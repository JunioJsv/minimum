package juniojsv.minimum

import android.content.Context

class Settings(context: Context) {

    private val settings = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun putBoolean(key: String, boolean: Boolean) {
        settings.edit().apply {
            putBoolean(key, boolean).commit()
        }.apply()
    }

    fun getBoolean(key: String): Boolean {
        return settings.getBoolean(key, false)
    }

}