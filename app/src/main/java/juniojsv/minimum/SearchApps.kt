package juniojsv.minimum

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import juniojsv.minimum.utilities.SortListOfApps
import java.util.*

class SearchApps internal constructor(private val minimum: MinimumActivity) : AsyncTask<Void, Void, MutableList<App>>() {
    private val packageManager = minimum.packageManager

    override fun onPreExecute() {
        super.onPreExecute()
        minimum.onSearchAppsStarting()
    }

    override fun doInBackground(vararg voids: Void): MutableList<App>? {

        return ArrayList<App>().apply {
            packageManager.getInstalledApplications(0).forEach {

                val intent: Intent? = packageManager.getLaunchIntentForPackage(it.packageName)

                if (intent != null && it.packageName != BuildConfig.APPLICATION_ID) {
                    intent.apply {
                        action = Intent.ACTION_MAIN
                        addCategory(Intent.CATEGORY_LAUNCHER)
                    }

                    val packageLabel: String = it.loadLabel(packageManager).toString()
                    val icon: Drawable = it.loadIcon(packageManager)
                    val packageName: String = it.packageName

                    add(App(packageLabel, icon, intent, packageName))
                }
            }
            SortListOfApps(this)
        }
    }

    override fun onPostExecute(newAppsList: MutableList<App>) {
        super.onPostExecute(newAppsList)
        minimum.onSearchAppsFinished(newAppsList)
    }
}