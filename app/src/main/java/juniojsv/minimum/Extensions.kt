package juniojsv.minimum

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView

fun AppCompatActivity.setActivityThemeByPreferences(preferences: SharedPreferences) {
    fun setThemeByAccentColor(red: Int, green: Int, blue: Int, default: Int) {
        when (preferences.getString(
            getString(R.string.pref_theme_accent_color_key),
            getString(R.string.pref_theme_accent_color_default)
        )) {
            getString(R.string.pref_theme_accent_color_red) -> setTheme(red)
            getString(R.string.pref_theme_accent_color_blue) -> setTheme(blue)
            getString(R.string.pref_theme_accent_color_green) -> setTheme(green)
            else -> setTheme(default)
        }
    }

    if (preferences.getBoolean(getString(R.string.pref_activate_dark_mode_key), false))
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

fun SearchView.clear() = setQuery("", true)
