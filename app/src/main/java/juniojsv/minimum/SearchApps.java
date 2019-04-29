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
    private SearchAppsListener searchAppsListener;

    SearchApps(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    void setListener(SearchAppsListener searchAppsListener) {
        this.searchAppsListener = searchAppsListener;
    }

    @Override
    protected void onPreExecute() {
        searchAppsListener.onAppsLoadingStarting();
        super.onPreExecute();
    }

    @Override
    protected List<App> doInBackground(Void... voids) {

        List<ApplicationInfo> appsInstalled = packageManager.getInstalledApplications(0);
        List<App> appsListCache = new ArrayList<>();

        for (ApplicationInfo appInfo : appsInstalled) {
            String packageLabel = appInfo.loadLabel(packageManager).toString();
            Drawable icon = appInfo.loadIcon(packageManager);
            Intent intent = packageManager.getLaunchIntentForPackage(appInfo.packageName);
            String packageName = appInfo.packageName;

            if (intent != null && !appInfo.packageName.equals(BuildConfig.APPLICATION_ID)) {

                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);

                appsListCache.add(new App(packageLabel, icon, intent, packageName));
            }
        }

        new SortListOfApps(appsListCache);

        return appsListCache;
    }

    @Override
    protected void onPostExecute(List<App> appsList) {
        super.onPostExecute(appsList);
        searchAppsListener.onAppsLoadingFinished();
    }
}