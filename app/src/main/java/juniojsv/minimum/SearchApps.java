package juniojsv.minimum;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
        List<App> appsListCache = new ArrayList<>();

        for (ApplicationInfo appInfo : appsInstalled) {
            String packageLabel = appInfo.loadLabel(pkgManager).toString();
            Drawable icon = appInfo.loadIcon(pkgManager);
            Intent intent = pkgManager.getLaunchIntentForPackage(appInfo.packageName);
            String packageName = appInfo.packageName;

            if (intent != null && !appInfo.packageName.equals(BuildConfig.APPLICATION_ID)) {

                if (packageLabel.charAt(0) != Character.toUpperCase(packageLabel.charAt(0))) {
                    packageLabel = packageLabel.substring(0, 1).toUpperCase() + packageLabel.substring(1);
                }

                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);

                appsListCache.add(new App(packageLabel, icon, intent, packageName));
            }
        }

        Collections.sort(appsListCache, new Comparator<App>() {
            @Override
            public int compare(App app1, App app2) {
                String app1Label = app1.getPackageLabel();
                String app2Label = app2.getPackageLabel();
                return app1Label.compareTo(app2Label);
            }
        });
        return appsListCache;
    }

    @Override
    protected void onPostExecute(List<App> appsList) {
        super.onPostExecute(appsList);
        Minimum.setAppsList(appsList);
        Minimum.startAdapter();
        Minimum.progressBar.setVisibility(View.GONE);
    }
}