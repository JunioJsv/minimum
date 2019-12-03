package juniojsv.minimum

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import juniojsv.minimum.extension.isNull
import kotlinx.android.synthetic.main.minimum_activity.*

class SettingsManager(activity: AppCompatActivity) {
    private val context = activity.applicationContext

    init {
        activities.find {
            it.localClassName == activity.localClassName
        }?.let {
            replace -> activities[activities.indexOf(replace)] = activity
        }?: activities.add(activity)
        if (preferences.isNull)
            preferences = context.getSharedPreferences("${context.packageName}.preferences", 0).apply {
                registerOnSharedPreferenceChangeListener { _, key ->
                    when(key) {
                        KEY_DARK_MODE ->
                            activities.forEach { activity ->
                                when(activity::class) {
                                    MinimumActivity::class -> {
                                        (activity as MinimumActivity).apply {
                                            unregisterReceiver(broadcastReceiver)
                                            recreate()
                                        }
                                    }
                                    else -> activity.recreate()
                                }
                            }
                        KEY_FAST_SCROLL ->
                            activities.find { activity -> activity::class == MinimumActivity::class }!!
                                    .apps_list_view.isFastScrollEnabled = preferences!!
                                    .getBoolean(KEY_FAST_SCROLL, false)
                    }
                }
            }
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
        const val KEY_FAST_SCROLL: String = "fast_scroll"
    }

}