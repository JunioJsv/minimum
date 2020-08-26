package juniojsv.minimum

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PreferencesEventHandler(private val listener: Listener) : BroadcastReceiver() {
    interface Listener {
        fun onPreferenceEvent(intent: Intent)
    }

    override fun onReceive(context: Context?, intent: Intent) =
            listener.onPreferenceEvent(intent)

    companion object {
        const val ACTION_FORCE_RECREATE = "juniojsv.minimum.ACTION_FORCE_RECREATE"
    }
}