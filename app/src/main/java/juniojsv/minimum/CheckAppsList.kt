package juniojsv.minimum

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import juniojsv.minimum.utilities.SortListOfApps

class CheckAppsList internal constructor(private val minimum: MinimumActivity) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        when(intent.action) {

            Intent.ACTION_PACKAGE_ADDED -> {
                val packageManager = context.packageManager
                val newApp = packageManager.getApplicationInfo(intent.dataString!!.substring(8), 0)
                val newAppIntent = packageManager.getLaunchIntentForPackage(newApp.packageName)

                if(newAppIntent != null && newApp.packageName != BuildConfig.APPLICATION_ID) {
                    minimum.apply {
                        appsList.add(
                                App(
                                        newApp.loadLabel(packageManager).toString(),
                                        newApp.loadIcon(packageManager),
                                        newAppIntent.apply {
                                            action = Intent.ACTION_MAIN
                                            addCategory(Intent.CATEGORY_LAUNCHER)
                                        },
                                        newApp.packageName
                                )
                        )
                        SortListOfApps(appsList)
                        notifyAdapter(true)
                    }
                }
            }

            Intent.ACTION_PACKAGE_REMOVED -> {
                val removedApp = intent.dataString!!.substring(8)
                var targetInList: App? = null

                minimum.apply {
                    appsList.apply {
                        forEach {
                            if(it.packageName == removedApp) targetInList = it
                        }
                        remove(targetInList)
                    }
                    notifyAdapter(true)
                }
            }
        }

    }
}
