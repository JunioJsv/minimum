package juniojsv.minimum

import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

fun ArrayList<Application>.sort() {
    forEach { application ->
        if (!application.label[0].isUpperCase())
            application.label = application.label[0].toUpperCase() + application.label.substring(1)
    }
    sortWith(Comparator { application, anotherApplication ->
        application.label.compareTo(anotherApplication.label)
    })
}

fun ArrayList<Application>.removeByPackage(packageName: String) {
    var target: Application? = null
    forEach { application ->
        if (application.packageName == packageName) target = application
    }
    target?.let { application -> remove(application) }
            ?: Log.e("removeByPackage", "Not found $packageName in ArrayList<App>")
}

fun AppCompatActivity.appearanceHandler(preferences: SharedPreferences) {
    fun accentColorHandler(red: Int, green: Int, blue: Int, default: Int) {
        when (preferences.getString("accent_color", "default")) {
            "red" -> setTheme(red)
            "green" -> setTheme(green)
            "blue" -> setTheme(blue)
            else -> setTheme(default)
        }
    }

    if (preferences.getBoolean("dark_mode", false))
        accentColorHandler(R.style.dark_mode_red, R.style.dark_mode_green, R.style.dark_mode_blue, R.style.dark_mode)
    else
        accentColorHandler(R.style.light_mode_red, R.style.light_mode_green, R.style.light_mode_blue, R.style.light_mode)
}