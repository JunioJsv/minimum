package juniojsv.minimum

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import juniojsv.minimum.utilities.SortListOfApps
import java.util.*

class SearchApps internal constructor(context: Context, private val minimumInterface: MinimumInterface) : AsyncTask<Void, Void, MutableList<App>>() {
    private val packageManager = context.packageManager

    override fun onPreExecute() {
        super.onPreExecute()
        minimumInterface.onSearchAppsStarting()
    }

    override fun doInBackground(vararg voids: Void): MutableList<App>? {

//        val installedApps: List<ApplicationInfo> = packageManager.getInstalledApplications(0)
//        val newAppsList: MutableList<App> = ArrayList()
//
//        for (app: ApplicationInfo in installedApps) {
//            val packageLabel: String = app.loadLabel(packageManager).toString()
//            val icon: Drawable = app.loadIcon(packageManager)
//            val intent: Intent? = packageManager.getLaunchIntentForPackage(app.packageName)
//            val packageName: String = app.packageName
//
//            if (intent != null && app.packageName != BuildConfig.APPLICATION_ID) {
//
//                intent.action = Intent.ACTION_MAIN
//                intent.addCategory(Intent.CATEGORY_LAUNCHER)
//
//                newAppsList.add(App(packageLabel, icon, intent, packageName))
//            }
//        }


        return ArrayList<App>().apply {
            packageManager.getInstalledApplications(0).forEach {
                val packageLabel: String = it.loadLabel(packageManager).toString()
                val icon: Drawable = it.loadIcon(packageManager)
                val intent: Intent? = packageManager.getLaunchIntentForPackage(it.packageName)
                val packageName: String = it.packageName

                if (intent != null && it.packageName != BuildConfig.APPLICATION_ID) {
                    intent.apply {
                        action = Intent.ACTION_MAIN
                        addCategory(Intent.CATEGORY_LAUNCHER)
                    }

                    add(App(packageLabel, icon, intent, packageName))
                }
            }
            SortListOfApps(this)
        }
    }

    override fun onPostExecute(newAppsList: MutableList<App>) {
        super.onPostExecute(newAppsList)
        minimumInterface.onSearchAppsFinished(newAppsList)
    }
}