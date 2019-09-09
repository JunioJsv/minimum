package juniojsv.minimum

import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.minimum_activity.*
import kotlinx.android.synthetic.main.search_header.view.*

class MinimumActivity : AppCompatActivity() {
    var appsList: MutableList<App> = ArrayList()
    private val filteredList: MutableList<App> = ArrayList()
    private var adapter: Adapter = Adapter(this, appsList)


    override fun onCreate(savedInstanceState: Bundle?) {
        applySettings()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.minimum_activity)

        apps_list_view.apply {
            addHeaderView(layoutInflater.inflate(
                    R.layout.search_header, apps_list_view, false))

            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                if(search_header.text.isNotEmpty() && position > 0) startActivity(
                        filteredList[position - 1].intent)
                else if(position > 0) startActivity(appsList[position - 1].intent)
            }

            onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
                if(search_header.text.isNotEmpty() && position > 0) {
                    val packageUri = Uri.parse("package:" + filteredList[position - 1].packageName)
                    val uninstall = Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri)
                    startActivity(uninstall)
                }
                else if(position > 0) {
                    val packageUri = Uri.parse("package:" + appsList[position - 1].packageName)
                    val uninstall = Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri)
                    startActivity(uninstall)
                }
                true
            }

            search_header.apply {
                addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(p0: Editable?) {
                    }

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        filteredList.clear()
                        if(p0!!.isNotEmpty()) {
                            appsList.forEach {
                                if (it.packageLabel.contains(p0, true)) filteredList.add(it)
                            }
                            this@MinimumActivity.adapter.changeList(filteredList)
                        } else this@MinimumActivity.adapter.changeList(appsList)
                        notifyAdapter()
                    }

                })
            }
        }

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
                TakePhoto(this)
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

    private fun applySettings() {
        if (Settings(this).getBoolean("dark_theme")) {
            setTheme(R.style.AppThemeDark)
        }
    }

    fun notifyAdapter(clearSearch: Boolean = false) {
        if (apps_list_view.adapter == null) {
            apps_list_view.adapter = adapter
        } else adapter.notifyDataSetChanged()
        if (clearSearch) apps_list_view.search_header.text.clear()
    }

    fun onSearchAppsStarting() {
        loading.visibility = View.VISIBLE
    }

    fun onSearchAppsFinished(newAppsList: MutableList<App>) {
        appsList.apply {
            if (isNotEmpty()) clear()
            addAll(newAppsList)
            notifyAdapter()
        }
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
