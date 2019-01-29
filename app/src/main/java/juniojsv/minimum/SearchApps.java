package juniojsv.minimum;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class SearchApps extends AsyncTask<Void, Void, List<App>> {
    private Context context;

    SearchApps(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Minimum.progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected List<App> doInBackground(Void... voids) {

        PackageManager pkgManager = context.getPackageManager();
        List<ApplicationInfo> appsInstalled = pkgManager.getInstalledApplications(0);
        List<App> appsList = new ArrayList<>();

        for (ApplicationInfo appInfo : appsInstalled) {
            String name = appInfo.loadLabel(pkgManager).toString();
            Drawable icon = appInfo.loadIcon(pkgManager);
            Intent intent = pkgManager.getLaunchIntentForPackage(appInfo.packageName);
            String uninstallName = appInfo.packageName;

            if (intent != null && !appInfo.packageName.equals(BuildConfig.APPLICATION_ID)) {
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);

                App appCache = new App(name, icon, intent, uninstallName);
                appsList.add(appCache);
            }
        }
        return appsList;
    }

    @Override
    protected void onPostExecute(List<App> appsList) {
        super.onPostExecute(appsList);
        Minimum.setAppsList(appsList);
        Minimum.startAdapter();
        Minimum.progressBar.setVisibility(View.GONE);
    }
}