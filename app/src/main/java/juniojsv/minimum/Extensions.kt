package juniojsv.minimum

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat

fun AppCompatActivity.setActivityThemeByPreferences(preferences: SharedPreferences) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        setTheme(R.style.AppThemeDynamicColors)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            val background = window.statusBarColor
            isAppearanceLightStatusBars = Color.BLACK.getContrast(background) >=
                    Color.WHITE.getContrast(background)
        }
        return
    }

    val isDarkMode = preferences.getBoolean(getString(R.string.pref_activate_dark_mode_key), false)
    val themes = if (isDarkMode) mapOf(
        getString(R.string.pref_theme_accent_color_default) to R.style.AppThemeDark,
        getString(R.string.pref_theme_accent_color_red) to R.style.AppThemeDark_Red,
        getString(R.string.pref_theme_accent_color_blue) to R.style.AppThemeDark_Blue,
        getString(R.string.pref_theme_accent_color_green) to R.style.AppThemeDark_Green,
    ) else mapOf(
        getString(R.string.pref_theme_accent_color_default) to R.style.AppThemeLight,
        getString(R.string.pref_theme_accent_color_red) to R.style.AppThemeLight_Red,
        getString(R.string.pref_theme_accent_color_blue) to R.style.AppThemeLight_Blue,
        getString(R.string.pref_theme_accent_color_green) to R.style.AppThemeLight_Green,
    )

    val theme = themes[preferences.getString(
        getString(R.string.pref_theme_accent_color_key),
        getString(R.string.pref_theme_accent_color_default)
    )]!!

    setTheme(theme)
}

fun Int.getContrast(other: Int) = ColorUtils.calculateContrast(this, other)

fun SearchView.clear() = setQuery("", true)
