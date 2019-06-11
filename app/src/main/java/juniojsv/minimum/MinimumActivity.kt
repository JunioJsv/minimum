package juniojsv.minimum

import android.content.Intent
import android.content.IntentFilter
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

class MinimumActivity : AppCompatActivity() {
    var appsList: MutableList<App> = ArrayList()
    private var adapter: Adapter = Adapter(this, appsList)
    private lateinit var camera: TakePhoto

    override fun onCreate(savedInstanceState: Bundle?) {
        applySettings()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.minimum_activity)

        SearchApps(this).apply {
            if (appsList.size == 0) execute()
        }

        registerReceiver(
                CheckAppsList(this).apply {
                    thisCheckAppsList = this
                },
                IntentFilter().apply {
                    addAction(Intent.ACTION_PACKAGE_ADDED)
                    addAction(Intent.ACTION_PACKAGE_REMOVED)
                    addDataScheme("package")
                }
        )

        thisActivity = this
        setOnClick()
    }

    private fun setOnClick() {
        apps_list_view.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            startActivity(appsList[position].intent)
        }
        apps_list_view.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
            val packageUri = Uri.parse("package:" + appsList[position].packageName)
            val uninstall = Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri)
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
                startActivity(Intent(Intent.ACTION_DIAL))
            }
            R.id.camera_shortcut -> {
                camera = TakePhoto(this).apply {
                    capture()
                }
            }
            R.id.setting_shortcut -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        //Nope
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == com.mindorks.paracamera.Camera.REQUEST_TAKE_PHOTO) and (Build.MANUFACTURER != "LGE")) {

            try {
                MoveFileTo(
                        File(camera.camera.cameraBitmapPath),
                        File(Environment.getExternalStorageDirectory().path + "/Minimum"))
                        .execute()
            } catch (error: Exception) {
                error.printStackTrace()
            }

        } else if ((requestCode == com.mindorks.paracamera.Camera.REQUEST_TAKE_PHOTO) and (Build.MANUFACTURER == "LGE")) {

            try {

                File(camera.camera.cameraBitmapPath).apply {
                    delete()
                }

            } catch (error: Exception) {
                error.printStackTrace()
            }

        }
    }

    private fun applySettings() {
        if (Settings(this).getBoolean("dark_theme")) {
            setTheme(R.style.AppThemeDark)
        }
    }

    fun notifyAdapter() {
        if (apps_list_view.adapter == null) {
            apps_list_view.adapter = adapter
        } else adapter.notifyDataSetChanged()
    }

    fun onSearchAppsStarting() {
        loading.visibility = View.VISIBLE
    }

    fun onSearchAppsFinished(newAppsList: MutableList<App>) {
        if (appsList.size != 0) {
            appsList.clear()
        }
        appsList.addAll(newAppsList)
        notifyAdapter()
        loading.visibility = View.GONE
    }

    companion object {
        private var thisActivity: MinimumActivity? = null
        private var thisCheckAppsList: CheckAppsList? = null
        fun recreate() {
            thisActivity?.apply {
                unregisterReceiver(thisCheckAppsList)
                recreate()
            }
        }
    }
}
