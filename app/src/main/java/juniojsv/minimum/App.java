package juniojsv.minimum;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public class App {
    private String packageLabel;
    private Drawable icon;
    private Intent intent;
    private String packageName;

    App(String packageLabel, Drawable icon, Intent intent, String packageName) {
        this.packageLabel = packageLabel;
        this.icon = icon;
        this.intent = intent;
        this.packageName = packageName;
    }

    public String getPackageLabel() {
        return packageLabel;
    }

    public void setPackageLabel(String packageLabel) {
        this.packageLabel = packageLabel;
    }

    public Drawable getIcon() {
        return icon;
    }

    public Intent getIntent() {
        return intent;
    }

    public String getPackageName() {
        return packageName;
    }
}