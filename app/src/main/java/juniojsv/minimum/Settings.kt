package juniojsv.minimum

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

class Settings(activity: AppCompatActivity) {
    private val context = activity.applicationContext

    init {
        activities.find {
            it.localClassName == activity.localClassName
        }?.let {
            replace -> activities[activities.indexOf(replace)] = activity
        }?: activities.add(activity)
    }

    private val settings = context.getSharedPreferences("${context.packageName}.settings", Context.MODE_PRIVATE).apply {
        registerOnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_DARK_MODE) {
                activities.forEach { activity ->
                    when(activity.localClassName) {
                        "MinimumActivity" -> {
                            (activity as MinimumActivity).apply {
                                unregisterReceiver(broadcastReceiver)
                                recreate()
                            }
                        }
                        else -> activity.recreate()
                    }
                }
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
        private val activities: ArrayList<AppCompatActivity> = ArrayList()
        const val KEY_DARK_MODE: String = "dark_theme"
    }

}