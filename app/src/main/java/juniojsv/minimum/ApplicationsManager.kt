package juniojsv.minimum

import android.content.Context
import android.content.Intent
import juniojsv.minimum.extension.sort

object ApplicationsManager {
    fun getAll(context: Context, onFinished: (applications: ArrayList<Application>) -> Unit) {
        Thread {
            val applications = arrayListOf<Application>()
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
                                applications.add(Application(label, icon, intent, packageName))
                            }
                        }
                    }
                }
            }
            applications.sort()
            onFinished(applications)
        }.start()
    }
}