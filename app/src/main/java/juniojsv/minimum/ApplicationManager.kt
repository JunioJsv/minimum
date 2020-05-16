package juniojsv.minimum

import android.content.Context
import android.content.Intent
import juniojsv.minimum.extension.sort

object ApplicationManager {
    fun getAll(context: Context, onFinished: (apps: ArrayList<App>) -> Unit) {
        Thread {
            val apps = arrayListOf<App>()
            val packageManager = context.packageManager

            packageManager.getInstalledApplications(0).forEach { application ->
                val intent = packageManager.getLaunchIntentForPackage(application.packageName)
                if (intent != null && application.packageName != BuildConfig.APPLICATION_ID) {
                    intent.apply {
                        action = Intent.ACTION_MAIN
                        categories.add(Intent.CATEGORY_LAUNCHER)
                    }
                    application.apply {
                        loadLabel(packageManager).toString().also { label ->
                            loadIcon(packageManager).also { icon ->
                                apps.add(App(label, icon, intent, packageName))
                            }
                        }
                    }
                }
            }
            apps.sort()
            onFinished(apps)
        }.start()
    }
}