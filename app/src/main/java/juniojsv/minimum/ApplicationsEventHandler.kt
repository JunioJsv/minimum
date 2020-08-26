package juniojsv.minimum

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

class ApplicationsEventHandler(private val listener: Listener) : BroadcastReceiver() {
    interface Listener {
        fun onApplicationAdded(intent: Intent)
        fun onApplicationRemoved(intent: Intent)
        fun onApplicationIsChanged(intent: Intent)
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED -> listener.onApplicationAdded(intent)
            Intent.ACTION_PACKAGE_REMOVED -> listener.onApplicationRemoved(intent)
            Intent.ACTION_PACKAGE_CHANGED -> listener.onApplicationIsChanged(intent)
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