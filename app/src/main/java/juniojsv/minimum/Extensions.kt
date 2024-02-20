package juniojsv.minimum

import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView

fun AppCompatActivity.setActivityThemeByPreferences(preferences: SharedPreferences) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        val isDarkMode = preferences.getBoolean(
            getString(R.string.pref_activate_dark_mode_key),
            false
        )
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode)
                AppCompatDelegate.MODE_NIGHT_YES else
                AppCompatDelegate.MODE_NIGHT_NO
        )
    }
    val themes = mapOf(
        getString(R.string.pref_theme_accent_color_default) to R.style.MinimumTheme,
        getString(R.string.pref_theme_accent_color_red) to R.style.MinimumTheme_Red,
        getString(R.string.pref_theme_accent_color_blue) to R.style.MinimumTheme_Blue,
        getString(R.string.pref_theme_accent_color_green) to R.style.MinimumTheme_Green,
    )

    themes[preferences.getString(
        getString(R.string.pref_theme_accent_color_key),
        getString(R.string.pref_theme_accent_color_default)
    )]?.let(::setTheme)
}

fun SearchView.clear() = setQuery("", true)
