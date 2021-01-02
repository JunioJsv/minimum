package juniojsv.minimum

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.appearanceHandler(preferences: SharedPreferences) {
    fun accentColorHandler(red: Int, green: Int, blue: Int, default: Int) {
        when (preferences.getString(PreferencesActivity.ACCENT_COLOR, "default")) {
            PreferencesActivity.ACCENT_COLOR_RED -> setTheme(red)
            PreferencesActivity.ACCENT_COLOR_BLUE -> setTheme(blue)
            PreferencesActivity.ACCENT_COLOR_GREEN -> setTheme(green)
            else -> setTheme(default)
        }
    }

    if (preferences.getBoolean(PreferencesActivity.DARK_MODE, false))
        accentColorHandler(
                R.style.AppThemeDark_Red, R.style.AppThemeDark_Green, R.style.AppThemeDark_Blue, R.style.AppThemeDark)
    else
        accentColorHandler(
                R.style.AppThemeLight_Red, R.style.AppThemeLight_Green, R.style.AppThemeLight_Blue, R.style.AppThemeLight)
}
