package juniojsv.minimum.applications

import android.content.Context
import android.content.pm.PackageManager
import juniojsv.minimum.BuildConfig
import juniojsv.minimum.R


object Applications {
    private lateinit var controller: Controller

    class Controller internal constructor(context: Context) {

        val applications = arrayListOf<Application>()

        init {
            context.apply {
                with(packageManager) {
                    getInstalledApplications(PackageManager.GET_META_DATA).forEach { info ->
                        val intent = getLaunchIntentForPackage(info.packageName)
                        if (intent != null && info.packageName != BuildConfig.APPLICATION_ID) {
                            val application = Application(context, info)
                            applications.add(application)
                        }
                    }
                    applications.sort()
                }
            }
        }

        fun addApplication(application: Application) = applications.apply {
            add(application)
            sort()
        }

        fun removeApplication(application: Application) = applications.apply {
            remove(application)
        }

        fun removeApplicationAt(index: Int) = applications.apply {
            removeAt(index)
        }
    }

    fun getInstance(context: Context): Controller {
        return if (this::controller.isInitialized)
            controller
        else {
            controller = Controller(context)
            controller
        }
    }
}