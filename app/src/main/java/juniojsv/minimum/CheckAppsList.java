package juniojsv.minimum;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import java.util.List;

import juniojsv.minimum.Utilities.SortListOfApps;

public class CheckAppsList extends BroadcastReceiver{
    private Context context;
    private Intent intent;
    private MinimumInterface minimumInterface;

    CheckAppsList(MinimumInterface minimumInterface) {
        this.minimumInterface = minimumInterface;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        this.intent = intent;

        if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
            addAppInList(minimumInterface.getAppsList());
        }

        if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
            removeAppOfList(minimumInterface.getAppsList());
        }

    }

    private void addAppInList(List<App> targetList) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo appInfo = null;

        try {
            appInfo = packageManager.getApplicationInfo(intent.getDataString().substring(8), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String packageLabel = appInfo.loadLabel(packageManager).toString();
        Drawable icon = appInfo.loadIcon(packageManager);
        Intent intent = packageManager.getLaunchIntentForPackage(appInfo.packageName);
        String packageName = appInfo.packageName;

        if (intent != null && !appInfo.packageName.equals(BuildConfig.APPLICATION_ID)) {

            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            targetList.add(new App(packageLabel, icon, intent, packageName));
            new SortListOfApps(targetList);
            minimumInterface.notifyAdapter();

        }
    }

    private void removeAppOfList(List<App> targetList) {
        App targetApp = null;

        for (App app : targetList) {
            if (app.getPackageName().equals(intent.getDataString().substring(8))) {
                targetApp = app;
            }
        }

        targetList.remove(targetApp);
        minimumInterface.notifyAdapter();
    }
}
