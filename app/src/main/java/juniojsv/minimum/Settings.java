package juniojsv.minimum;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Settings extends PreferenceActivity {
    public static boolean needRestart;
    private SharedPreferences checkBoxDarkTheme;
    private SharedPreferences.Editor settingEditor = Minimum.settings.edit();
    private final boolean DARK_THEME_PREF = Minimum.settings.getBoolean("dark_theme", false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applySettings();
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
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
        if (Minimum.settings.getBoolean("dark_theme", false)) {
            setTheme(R.style.AppThemeDark);
        }
    }
}
