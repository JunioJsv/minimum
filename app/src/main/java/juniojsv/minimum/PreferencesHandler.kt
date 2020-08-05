package juniojsv.minimum

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class PreferencesHandler(private val context: Context) : SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            "dark_mode", "accent_color" ->
                LocalBroadcastManager.getInstance(context)
                        .sendBroadcast(Intent(PreferencesEventHandler.ACTION_FORCE_RECREATE).apply {
                            putExtra("activity", "all")
                        })

            "grid_view", "grid_view_columns" ->
                LocalBroadcastManager.getInstance(context)
                        .sendBroadcast(Intent(PreferencesEventHandler.ACTION_FORCE_RECREATE).apply {
                            putExtra("activity", "minimum")
                        })
        }
    }
}