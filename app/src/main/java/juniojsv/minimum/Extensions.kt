package juniojsv.minimum

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap

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
        accentColorHandler(
                R.style.AppThemeDark_Red, R.style.AppThemeDark_Green, R.style.AppThemeDark_Blue, R.style.AppThemeDark)
    else
        accentColorHandler(
                R.style.AppThemeLight_Red, R.style.AppThemeLight_Green, R.style.AppThemeLight_Blue, R.style.AppThemeLight)
}

fun ApplicationInfo.toApplication(packageManager: PackageManager, iconSize: Int, isNew: Boolean = false): Application? {
    with(packageManager) {
        val intent = getLaunchIntentForPackage(packageName)
        return if (intent != null && packageName != BuildConfig.APPLICATION_ID) {
            with(intent) {
                action = Intent.ACTION_MAIN
                categories.add(Intent.CATEGORY_LAUNCHER)
            }
            Application(
                    loadLabel(this).toString(),
                    loadIcon(this).toBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888),
                    intent,
                    packageName,
                    isNew
            )
        } else null
    }
}