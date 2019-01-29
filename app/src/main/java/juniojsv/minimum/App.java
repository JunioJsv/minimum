package juniojsv.minimum;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public class App {
    private String name;
    private Drawable icon;
    private Intent intent;
    private String uninstallName;

    App(String name, Drawable icon, Intent intent, String uninstallName) {
        this.name = name;
        this.icon = icon;
        this.intent = intent;
        this.uninstallName = uninstallName;
    }

    public String getName() {
        return name;
    }

    public Drawable getIcon() {
        return icon;
    }

    public Intent getIntent() {
        return intent;
    }

    public String getUninstallName() {
        return uninstallName;
    }
}