package juniojsv.minimum

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import juniojsv.minimum.utilities.SortListOfApps
import java.util.*

class SearchApps internal constructor(private val packageManager: PackageManager, private val minimumInterface: MinimumInterface) : AsyncTask<Void, Void, MutableList<App>>() {

    override fun onPreExecute() {
        super.onPreExecute()
        minimumInterface.onSearchAppsStarting()
    }

    override fun doInBackground(vararg voids: Void): MutableList<App>? {

        val installedApps: List<ApplicationInfo> = packageManager.getInstalledApplications(0)
        val newAppsList: MutableList<App> = ArrayList()

        for (app: ApplicationInfo in installedApps) {
            val packageLabel: String = app.loadLabel(packageManager).toString()
            val icon: Drawable = app.loadIcon(packageManager)
            val intent: Intent? = packageManager.getLaunchIntentForPackage(app.packageName)
            val packageName: String = app.packageName

            if (intent != null && app.packageName != BuildConfig.APPLICATION_ID) {

                intent.action = Intent.ACTION_MAIN
                intent.addCategory(Intent.CATEGORY_LAUNCHER)

                newAppsList.add(App(packageLabel, icon, intent, packageName))
            }
        }

        SortListOfApps(newAppsList)

        return newAppsList
    }

    override fun onPostExecute(newAppsList: MutableList<App>) {
        super.onPostExecute(newAppsList)
        minimumInterface.onSearchAppsFinished(newAppsList)
    }
}