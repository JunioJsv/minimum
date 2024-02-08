package juniojsv.minimum

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import juniojsv.minimum.features.preferences.PreferencesActivity

fun AppCompatActivity.setActivityThemeByPreferences(preferences: SharedPreferences) {
    fun setThemeByAccentColor(red: Int, green: Int, blue: Int, default: Int) {
        when (preferences.getString(PreferencesActivity.ACCENT_COLOR, "default")) {
            PreferencesActivity.ACCENT_COLOR_RED -> setTheme(red)
            PreferencesActivity.ACCENT_COLOR_BLUE -> setTheme(blue)
            PreferencesActivity.ACCENT_COLOR_GREEN -> setTheme(green)
            else -> setTheme(default)
        }
    }

    if (preferences.getBoolean(PreferencesActivity.DARK_MODE, false))
        setThemeByAccentColor(
            R.style.AppThemeDark_Red,
            R.style.AppThemeDark_Green,
            R.style.AppThemeDark_Blue,
            R.style.AppThemeDark
        )
    else
        setThemeByAccentColor(
            R.style.AppThemeLight_Red,
            R.style.AppThemeLight_Green,
            R.style.AppThemeLight_Blue,
            R.style.AppThemeLight
        )
}

fun SearchView.setIconified() {
    if (!isIconified) {
        setQuery("", true)
        isIconified = true
    }
}
