package juniojsv.minimum;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
    public static boolean needRestart;
    private SharedPreferences checkBoxDarkTheme;
    private SharedPreferences.Editor settingEditor = MinimumActivity.settings.edit();
    private final boolean DARK_THEME_PREF = MinimumActivity.settings.getBoolean("dark_theme", false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applySettings();
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_activity);
        checkBoxDarkTheme = findPreference("prefs_dark_theme").getSharedPreferences();
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateSettings();
    }

    private void updateSettings() {
        if (checkBoxDarkTheme.getBoolean("prefs_dark_theme", false) != DARK_THEME_PREF) {
            settingEditor.putBoolean("dark_theme", checkBoxDarkTheme.getBoolean("prefs_dark_theme", false));
            needRestart = true;
        }

        if (needRestart) {
            settingEditor.apply();
            recreate();
        }
    }

    private void applySettings() {
        if (MinimumActivity.settings.getBoolean("dark_theme", false)) {
            setTheme(R.style.AppThemeDark);
        }
    }
}
