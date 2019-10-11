package juniojsv.minimum

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import juniojsv.minimum.extension.arrayList.sort
import java.lang.ref.WeakReference

class GetApps internal constructor(activity: WeakReference<AppCompatActivity>, private val onFinished: (apps: ArrayList<App>) -> Unit) : AsyncTask<Void, Void, ArrayList<App>>() {
    private val packageManager = activity.get()!!.packageManager

    override fun doInBackground(vararg voids: Void): ArrayList<App> {

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
            sort()
        }
    }

    override fun onPostExecute(apps: ArrayList<App>) {
        super.onPostExecute(apps)
        onFinished.invoke(apps)
    }
}