package juniojsv.minimum.features.applications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

class ApplicationsEventsBroadcastReceiver(private val listener: Listener) : BroadcastReceiver() {
    interface Listener {
        fun onApplicationAdded(intent: Intent)
        fun onApplicationRemoved(intent: Intent)
        fun onApplicationUpdated(intent: Intent)
    }

    override fun onReceive(context: Context, intent: Intent) {
        val isReplacingPackage = intent
            .getBooleanExtra(Intent.EXTRA_REPLACING, false)
        when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED -> {
                if (!isReplacingPackage) {
                    listener.onApplicationAdded(intent)
                } else {
                    listener.onApplicationUpdated(intent)
                }
            }

            Intent.ACTION_PACKAGE_REMOVED -> {
                if (!isReplacingPackage)
                    listener.onApplicationRemoved(intent)
            }
        }
    }

    companion object {
        val DEFAULT_INTENT_FILTER = IntentFilter().apply {
            addDataScheme("package")
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
        }
    }
}