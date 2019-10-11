package juniojsv.minimum

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

class Settings(activity: AppCompatActivity) {
    private val context = activity.applicationContext

    init {
        if (!activitys.contains(activity)) activitys.add(activity)
    }

    private val settings = context.getSharedPreferences("${context.packageName}.settings", Context.MODE_PRIVATE).apply {
        registerOnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_DARK_MODE) {
                activitys.forEach { it.recreate() }
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

    companion object {
        private val activitys: ArrayList<AppCompatActivity> = ArrayList()
        const val KEY_DARK_MODE: String = "dark_theme"
    }

}