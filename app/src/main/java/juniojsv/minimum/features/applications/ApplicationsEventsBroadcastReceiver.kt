package juniojsv.minimum.features.applications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

class ApplicationsEventsBroadcastReceiver(private val callbacks: Callbacks) : BroadcastReceiver() {
    interface Callbacks {
        fun onApplicationAdded(intent: Intent)
        fun onApplicationRemoved(intent: Intent)
        fun onApplicationUpdated(intent: Intent)

        fun onApplicationDisabled(intent: Intent)

        fun getApplicationIndexByPackageName(packageName: String): Int
    }

    override fun onReceive(context: Context, intent: Intent) {
        val isReplacingPackage = intent
            .getBooleanExtra(Intent.EXTRA_REPLACING, false)
        when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED -> {
                if (isReplacingPackage) {
                    callbacks.onApplicationUpdated(intent)
                } else {
                    callbacks.onApplicationAdded(intent)
                }
            }

            Intent.ACTION_PACKAGE_REMOVED -> {
                if (!isReplacingPackage)
                    callbacks.onApplicationRemoved(intent)
            }

            Intent.ACTION_PACKAGE_CHANGED -> {
                val packageManager = context.packageManager
                intent.data?.schemeSpecificPart?.let { packageName ->
                    val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                    if (launchIntent == null) {
                        callbacks.onApplicationDisabled(intent)
                    } else if (callbacks.getApplicationIndexByPackageName(packageName) == -1) {
                        callbacks.onApplicationAdded(intent)
                    }
                }
            }
        }
    }

    companion object {
        val DEFAULT_INTENT_FILTER = IntentFilter().apply {
            addDataScheme("package")
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
        }
    }
}