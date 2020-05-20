package juniojsv.minimum

import android.content.*
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
import androidx.preference.PreferenceManager
import juniojsv.minimum.PreferencesActivity.Companion.registerActivity
import kotlinx.android.synthetic.main.minimum_activity.*
import kotlinx.android.synthetic.main.search_header_view.view.*

class MinimumActivity : AppCompatActivity() {
    private var applications: ArrayList<Application> = ArrayList()
    private val filteredApplications: ArrayList<Application> = ArrayList()
    private var applicationsAdapter: ApplicationsAdapter = ApplicationsAdapter(this, applications)
    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_PACKAGE_ADDED -> {
                    val packageManager = context.packageManager
                    val application = packageManager.getApplicationInfo(intent.dataString!!.substring(8), 0)
                    val applicationIntent = packageManager.getLaunchIntentForPackage(application.packageName)

                    if (applicationIntent != null && application.packageName != BuildConfig.APPLICATION_ID) {
                        applicationIntent.apply {
                            action = Intent.ACTION_MAIN
                            categories.add(Intent.CATEGORY_LAUNCHER)
                        }
                        application.apply {
                            loadLabel(packageManager).toString().also { label ->
                                loadIcon(packageManager).also { icon ->
                                    with(applications) {
                                        add(Application(label, icon, applicationIntent, packageName, true))
                                        sort()
                                    }
                                }
                            }
                        }
                        notifyAdapter(true)
                    }
                }

                Intent.ACTION_PACKAGE_REMOVED -> {
                    applications.removeByPackage(
                            intent.dataString!!.substring(8)
                    )
                    notifyAdapter(true)
                }
            }
        }
    }
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        appearanceHandler(preferences)
        setContentView(R.layout.minimum_activity)
        registerActivity(this)

        applications_view.apply {
            addHeaderView(layoutInflater.inflate(
                    R.layout.search_header_view, applications_view, false))

            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                fun launchIntent(application: Application): Intent =
                        application.run {
                            if (newlyInstalled) {
                                newlyInstalled = false
                                notifyAdapter()
                            }
                            intent
                        }

                if (search_header_view.text.isNotEmpty() && position > 0)
                    startActivity(launchIntent(filteredApplications[position - 1]))
                else if (position > 0)
                    startActivity(launchIntent(applications[position - 1]))
            }

            onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
                fun uninstallIntent(application: Application): Intent =
                        Intent(
                                Intent.ACTION_DELETE,
                                Uri.parse("package:${application.packageName}")
                        )

                if (search_header_view.text.isNotEmpty() && position > 0)
                    startActivity(uninstallIntent(filteredApplications[position - 1]))
                else if (position > 0) {
                    startActivity(uninstallIntent(applications[position - 1]))
                }
                true
            }

            search_header_view.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {}

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(string: CharSequence, start: Int, before: Int, count: Int) {
                    filteredApplications.clear()
                    if (string.isNotEmpty()) {
                        applications.forEach { application ->
                            if (application.label.contains(string, true)) filteredApplications.add(application)
                        }
                        this@MinimumActivity.applicationsAdapter.changeList(filteredApplications)
                    } else this@MinimumActivity.applicationsAdapter.changeList(applications)
                    notifyAdapter()
                }
            })
        }

        ApplicationsManager.getAll(applicationContext) { apps ->
            runOnUiThread {
                this.applications.addAll(apps)
                notifyAdapter()
                loading_view.visibility = View.GONE
            }
        }

        registerReceiver(
                broadcastReceiver,
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
            R.id.preferences_shortcut -> {
                startActivity(Intent(this, PreferencesActivity::class.java))
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
        applications_view.adapter?.let { applicationsAdapter.notifyDataSetChanged() }
                ?: applications_view.also { it.adapter = applicationsAdapter }
        if (clearSearch) applications_view.search_header_view.text.clear()
    }
}
