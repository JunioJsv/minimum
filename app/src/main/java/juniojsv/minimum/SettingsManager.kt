package juniojsv.minimum

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.minimum_activity.*

class SettingsManager(activity: AppCompatActivity) {
    init {
        registerActivity(activity)
        if (preferences == null)
            with(activity.applicationContext) {
                preferences = getSharedPreferences("$packageName.preferences", 0).apply {
                    registerOnSharedPreferenceChangeListener { _, key ->
                        when (key) {
                            KEY_DARK_MODE -> activities.forEach { activity ->
                                activity.recreate()
                            }
                        }
                    }
                }
            }
    }

    private fun registerActivity(activity: AppCompatActivity) {
        activities.find { registered -> activity.localClassName == registered.localClassName }?.let { target ->
            activities[activities.indexOf(target)] = activity
        } ?: activities.add(activity)
    }

    fun putBoolean(key: String, boolean: Boolean) =
            preferences!!.edit().apply {
                putBoolean(key, boolean).commit()
            }.apply()

    fun getBoolean(key: String): Boolean = preferences!!.getBoolean(key, false)

    companion object {
        private var preferences: SharedPreferences? = null
        private val activities: ArrayList<AppCompatActivity> = ArrayList()
        const val KEY_DARK_MODE: String = "dark_theme"
    }

}