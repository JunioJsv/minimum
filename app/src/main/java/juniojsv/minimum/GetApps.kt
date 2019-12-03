package juniojsv.minimum

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import juniojsv.minimum.extension.isNull
import juniojsv.minimum.extension.sort
import java.lang.ref.WeakReference

class GetApps(context: WeakReference<Context>, private val onFinished: (apps: ArrayList<App>) -> Unit) : AsyncTask<Void, Void, ArrayList<App>>() {
    private val packageManager = context.get()!!.packageManager

    override fun doInBackground(vararg voids: Void): ArrayList<App> =
            ArrayList<App>().apply {
                packageManager.getInstalledApplications(0).forEach { info ->

                    val intent: Intent? = packageManager.getLaunchIntentForPackage(info.packageName)

                    if (!intent.isNull && info.packageName != BuildConfig.APPLICATION_ID) {
                        intent!!.apply {
                            action = Intent.ACTION_MAIN
                            addCategory(Intent.CATEGORY_LAUNCHER)
                        }
                        info.apply {
                            loadLabel(packageManager).toString().also { label ->
                                loadIcon(packageManager).also { icon ->
                                    packageName.also { packageName ->
                                        add(App(label, icon, intent, packageName))
                                    }
                                }
                            }
                        }
                    }
                }
                sort()
            }

    override fun onPostExecute(apps: ArrayList<App>) {
        super.onPostExecute(apps)
        onFinished.invoke(apps)
    }
}