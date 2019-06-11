package juniojsv.minimum

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

import juniojsv.minimum.utilities.SortListOfApps

class CheckAppsList internal constructor(private val minimum: MinimumActivity) : BroadcastReceiver() {
    private var context: Context? = null
    private var intent: Intent? = null

    override fun onReceive(context: Context, intent: Intent) {
        this.context = context
        this.intent = intent

        if (intent.action == Intent.ACTION_PACKAGE_ADDED) {
            addAppInList(minimum.appsList)
        }

        if (intent.action == Intent.ACTION_PACKAGE_REMOVED) {
            removeAppOfList(minimum.appsList)
        }

    }

    private fun addAppInList(targetList: MutableList<App>) {
        val packageManager = context!!.packageManager
        var app: ApplicationInfo? = null

        try {
            app = packageManager.getApplicationInfo(intent!!.dataString!!.substring(8), 0)
        } catch (exception: PackageManager.NameNotFoundException) {
            exception.printStackTrace()
        }

        val packageLabel = app!!.loadLabel(packageManager).toString()
        val icon = app.loadIcon(packageManager)
        val intent = packageManager.getLaunchIntentForPackage(app.packageName)
        val packageName = app.packageName

        if (intent != null && app.packageName != BuildConfig.APPLICATION_ID) {

            intent.action = Intent.ACTION_MAIN
            intent.addCategory(Intent.CATEGORY_LAUNCHER)

            targetList.add(App(packageLabel, icon, intent, packageName))
            SortListOfApps(targetList)
            minimum.notifyAdapter()

        }
    }

    private fun removeAppOfList(targetList: MutableList<App>) {
        var targetApp: App? = null

        for (app in targetList) {
            if (app.packageName == intent!!.dataString!!.substring(8)) {
                targetApp = app
            }
        }

        targetList.remove(targetApp)
        minimum.notifyAdapter()
    }
}
