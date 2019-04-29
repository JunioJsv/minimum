package juniojsv.minimum;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CheckBox;

public class Settings extends AppCompatActivity {
    public static boolean needRestart;
    private CheckBox checkBoxDarkTheme;
    private SharedPreferences.Editor settingEditor = Minimum.settings.edit();
    private final boolean DARK_THEME_PREF = Minimum.settings.getBoolean("dark_theme", false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        setTitle("Settings");
        checkBoxDarkTheme = findViewById(R.id.prefs_dark_theme);
        checkBoxDarkTheme.setChecked(DARK_THEME_PREF);
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateSettings();
    }

    private void updateSettings() {
        if (checkBoxDarkTheme.isChecked() != DARK_THEME_PREF) {
            settingEditor.putBoolean("dark_theme", checkBoxDarkTheme.isChecked());
            needRestart = true;
        }

        if (needRestart) {
            settingEditor.apply();
        }
    }
}
