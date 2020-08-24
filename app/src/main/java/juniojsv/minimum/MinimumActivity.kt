package juniojsv.minimum

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import juniojsv.minimum.PreferencesEventHandler.Companion.ACTION_FORCE_RECREATE

class MinimumActivity : AppCompatActivity(), PreferencesEventHandler.Listener {
    private lateinit var preferences: SharedPreferences
    private val preferencesEventHandler = PreferencesEventHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        appearanceHandler(preferences)
        setContentView(R.layout.minimum_activity)
        supportFragmentManager.commit {
            replace(R.id.mApplicationsFragment, ApplicationsFragment())
        }
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(preferencesEventHandler, PreferencesEventHandler.DEFAULT_INTENT_FILTER)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.minimum_shortcuts, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mDial ->
                startActivity(Intent(Intent.ACTION_DIAL))
            R.id.mCamera ->
                startActivity(
                        Intent.createChooser(
                                Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA),
                                getString(R.string.take_pictures_with)
                        )
                )
            R.id.mPreferences ->
                startActivity(Intent(this, PreferencesActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(preferencesEventHandler)
    }

    override fun onBackPressed() {
        // Nothings
    }

    override fun onPreferenceEvent(intent: Intent) {
        when (intent.action) {
            ACTION_FORCE_RECREATE -> {
                val value = intent.getStringExtra("activity")
                if (value == "all" || value == "minimum")
                    recreate()
            }
        }
    }

}
