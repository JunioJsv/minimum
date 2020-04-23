package juniojsv.minimum

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import juniojsv.minimum.SettingsManager.Companion.KEY_DARK_MODE
import juniojsv.minimum.SettingsManager.Companion.KEY_FAST_SCROLL
import juniojsv.minimum.extension.isNull
import juniojsv.minimum.extension.removeByPackage
import juniojsv.minimum.extension.sort
import kotlinx.android.synthetic.main.minimum_activity.*
import kotlinx.android.synthetic.main.search_header.view.*
import java.lang.ref.WeakReference

class MinimumActivity : AppCompatActivity() {
    private var apps: ArrayList<App> = ArrayList()
    private val filteredApps: ArrayList<App> = ArrayList()
    private var adapter: Adapter = Adapter(this, apps)
    private lateinit var settings: SettingsManager
    private lateinit var broadcastReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {

        SettingsManager(this).also { settings = it }
        if (settings.getBoolean(KEY_DARK_MODE)) setTheme(R.style.dark_mode)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.minimum_activity)

        apps_list_view.apply {
            addHeaderView(layoutInflater.inflate(
                    R.layout.search_header, apps_list_view, false))

            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                if (search_header.text.isNotEmpty() && position > 0)
                    startActivity(filteredApps[position - 1].intent)
                else if (position > 0)
                    startActivity(apps[position - 1].intent)
            }

            onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
                if (search_header.text.isNotEmpty() && position > 0) {
                    val packageName = Uri.parse("package:" + filteredApps[position - 1].packageName)
                    val uninstall = Intent(Intent.ACTION_DELETE, packageName)
                    startActivity(uninstall)
                } else if (position > 0) {
                    val packageName = Uri.parse("package:" + apps[position - 1].packageName)
                    val uninstall = Intent(Intent.ACTION_DELETE, packageName)
                    startActivity(uninstall)
                }
                true
            }

            search_header.apply {
                addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(p0: Editable?) {}

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        filteredApps.clear()
                        if (p0!!.isNotEmpty()) {
                            apps.forEach { app ->
                                if (app.label.contains(p0, true)) filteredApps.add(app)
                            }
                            this@MinimumActivity.adapter.changeList(filteredApps)
                        } else this@MinimumActivity.adapter.changeList(apps)
                        notifyAdapter()
                    }

                })
            }

            apps_list_view.isFastScrollEnabled = settings.getBoolean(KEY_FAST_SCROLL)
        }

        GetApps(WeakReference(this)) { apps ->
            this.apps.apply {
                if (isNotEmpty()) clear()
                addAll(apps)
                notifyAdapter()
            }
            loading.visibility = View.GONE
        }.apply {
            if(apps.isEmpty()) execute()
        }

        registerReceiver(
                object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        when(intent.action) {
                            Intent.ACTION_PACKAGE_ADDED -> {
                                val appAdded = context.packageManager.getApplicationInfo(intent.dataString!!.substring(8), 0)
                                val appIntentAdded = context.packageManager.getLaunchIntentForPackage(appAdded.packageName)

                                if(!appIntentAdded.isNull && appAdded.packageName != BuildConfig.APPLICATION_ID) {
                                    apps.apply {
                                        add(App(
                                                appAdded.loadLabel(context.packageManager).toString(),
                                                appAdded.loadIcon(context.packageManager),
                                                appIntentAdded!!.apply {
                                                    action = Intent.ACTION_MAIN
                                                    addCategory(Intent.CATEGORY_LAUNCHER)
                                                },
                                                appAdded.packageName
                                        ))
                                        sort()
                                    }
                                }
                                notifyAdapter(true)
                            }

                            Intent.ACTION_PACKAGE_REMOVED -> {
                                apps.removeByPackage(
                                        intent.dataString!!.substring(8)
                                )
                                notifyAdapter(true)
                            }
                        }
                    }
                }.also { broadcastReceiver = it },
                IntentFilter().apply {
                    addAction(Intent.ACTION_PACKAGE_ADDED)
                    addAction(Intent.ACTION_PACKAGE_REMOVED)
                    addDataScheme("package")
                }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.minimum_shortcuts, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.dial_shortcut -> {
                startActivity(Intent(Intent.ACTION_DIAL))
            }
            R.id.camera_shortcut -> {
                startActivity(
                        Intent.createChooser(
                                Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA),
                                getString(R.string.take_pictures_with)
                        )
                )
            }
            R.id.setting_shortcut -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        // Nope
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }

    private fun notifyAdapter(clearSearch: Boolean = false) {
        apps_list_view.adapter?.let { adapter.notifyDataSetChanged() } ?:
                apps_list_view.also { it.adapter = adapter }
        if (clearSearch) apps_list_view.search_header.text.clear()
    }
}
