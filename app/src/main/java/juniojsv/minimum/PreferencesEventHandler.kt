package juniojsv.minimum

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

class PreferencesEventHandler(private val listener: Listener) : BroadcastReceiver() {

    interface Listener {
        fun onForceRecreate(intent: Intent)
    }

    override fun onReceive(context: Context?, intent: Intent) {
        when (intent.action) {
            ACTION_FORCE_RECREATE -> listener.onForceRecreate(intent)
        }
    }

    companion object {
        val DEFAULT_INTENT_FILTER = IntentFilter().apply {
            addAction(ACTION_FORCE_RECREATE)
        }

        const val ACTION_FORCE_RECREATE = "juniojsv.minimum.ACTION_FORCE_RECREATE"
    }
}