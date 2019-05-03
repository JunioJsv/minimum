package juniojsv.minimum;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import juniojsv.minimum.Utilities.SortListOfApps;

public class SearchApps extends AsyncTask<Void, Void, List<App>> {
    private PackageManager packageManager;
    private MinimumInterface minimumInterface;

    SearchApps(PackageManager packageManager, MinimumInterface minimumInterface) {
        this.packageManager = packageManager;
        this.minimumInterface = minimumInterface;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        minimumInterface.onSearchAppsStarting();
    }

    @Override
    protected List<App> doInBackground(Void... voids) {

        List<ApplicationInfo> installedApps = packageManager.getInstalledApplications(0);
        List<App> newAppsList = new ArrayList<>();

        for (ApplicationInfo app : installedApps) {
            String packageLabel = app.loadLabel(packageManager).toString();
            Drawable icon = app.loadIcon(packageManager);
            Intent intent = packageManager.getLaunchIntentForPackage(app.packageName);
            String packageName = app.packageName;

            if (intent != null && !app.packageName.equals(BuildConfig.APPLICATION_ID)) {

                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);

                newAppsList.add(new App(packageLabel, icon, intent, packageName));
            }
        }

        new SortListOfApps(newAppsList);

        return newAppsList;
    }

    @Override
    protected void onPostExecute(List<App> newAppsList) {
        super.onPostExecute(newAppsList);
        minimumInterface.onSearchAppsFinished(newAppsList);
    }
}