package juniojsv.minimum

import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import juniojsv.minimum.utilities.MoveFileTo
import kotlinx.android.synthetic.main.minimum_activity.*
import java.io.File
import java.util.*

class MinimumActivity : AppCompatActivity(), MinimumInterface {
    override var appsList: MutableList<App> = ArrayList(0)
    private var adapter: Adapter = Adapter(this, appsList)
    private lateinit var checkAppsList: BroadcastReceiver
    private lateinit var takePhoto: TakePhoto

    override fun onCreate(savedInstanceState: Bundle?) {
        settings = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        applySettings()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.minimum_activity)

        val searchApps = SearchApps(this.packageManager, this)
        if(appsList.size == 0) searchApps.execute()

        checkAppsList = CheckAppsList(this)
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        intentFilter.addDataScheme("package")
        this.registerReceiver(checkAppsList, intentFilter)

        setOnClick()
    }

    private fun setOnClick() {
        appsListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val appIntent = appsList[position].intent
            startActivity(appIntent)
        }
        appsListView.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
            val pkgUri = Uri.parse("package:" + appsList[position].packageName)
            val uninstall = Intent(Intent.ACTION_UNINSTALL_PACKAGE, pkgUri)
            startActivity(uninstall)
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.minimum_shortcut, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.dial_shortcut -> {
                val dialIntent = Intent(Intent.ACTION_DIAL)
                startActivity(dialIntent)
            }
            R.id.camera_shortcut -> {
                takePhoto = TakePhoto(this)
                takePhoto.capture()
            }
            R.id.setting_shortcut -> {
                val settingIntent = Intent(this, SettingsActivity::class.java)
                startActivity(settingIntent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        //Nope
    }

    override fun onResume() {
        super.onResume()
        if (SettingsActivity.needRestart) {
            SettingsActivity.needRestart = false
            unregisterReceiver(checkAppsList)
            recreate()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == com.mindorks.paracamera.Camera.REQUEST_TAKE_PHOTO) and (Build.MANUFACTURER != "LGE")) {

            try {
                val moveFileTo = MoveFileTo(File(takePhoto.camera.cameraBitmapPath), File(Environment.getExternalStorageDirectory().path + "/Minimum"))
                moveFileTo.execute()
            } catch (error: Exception) {
                error.printStackTrace()
            }

        } else if ((requestCode == com.mindorks.paracamera.Camera.REQUEST_TAKE_PHOTO) and (Build.MANUFACTURER == "LGE")) {

            try {
                val file = File(takePhoto.camera.cameraBitmapPath)

                file.delete()

            } catch (error: Exception) {
                error.printStackTrace()
            }

        }
    }

    private fun applySettings() {
        if (settings.getBoolean("dark_theme", false)) {
            setTheme(R.style.AppThemeDark)
        }
    }

    override fun notifyAdapter() {
        if (appsListView.adapter == null) {
            appsListView.adapter = adapter
        } else
            adapter.notifyDataSetChanged()
    }

    override fun onSearchAppsStarting() {
        loading.visibility = View.VISIBLE
    }

    override fun onSearchAppsFinished(newAppsList: MutableList<App>) {
        if (appsList.size != 0) {
            appsList.clear()
        }
        appsList.addAll(newAppsList)
        notifyAdapter()
        loading.visibility = View.GONE
    }

    companion object {
        lateinit var settings: SharedPreferences
    }
}
