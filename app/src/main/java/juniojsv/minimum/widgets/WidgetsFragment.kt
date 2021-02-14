package juniojsv.minimum.widgets

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.WallpaperManager
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import juniojsv.minimum.R
import juniojsv.minimum.databinding.WidgetsFragmentBinding
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

class WidgetsFragment : Fragment(), CoroutineScope, WidgetsActionsDialog.Listener , WidgetAdapterHolder.HolderListener{
    private lateinit var binding: WidgetsFragmentBinding
    private lateinit var preferences: SharedPreferences
    private lateinit var widgetManager: AppWidgetManager
    private lateinit var widgetHost: AppWidgetHost
    lateinit var applicationContext: Context
    private val widgets = ArrayList<AppWidgetHostView>()
    private val widgetsAdapter = WidgetsAdapter(widgets, this)
    private val job = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.IO + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applicationContext = requireContext().applicationContext
        preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        widgetManager = AppWidgetManager.getInstance(applicationContext)
        widgetHost = AppWidgetHost(applicationContext, HOST_ID)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = WidgetsFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.mWidgets.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = widgetsAdapter
        }

        if (SDK_INT >= 26 && ActivityCompat.checkSelfPermission(applicationContext, READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            requestPermissions(arrayOf(READ_EXTERNAL_STORAGE), REQUEST_PERMISSIONS)
        } else {
            setUpWallpaper()
        }

        binding.mEditActions.setOnClickListener {
            WidgetsActionsDialog(this)
                    .show(parentFragmentManager, "WidgetsActions")
        }

        launch {
            preferences.getStringSet("widgets", setOf())?.forEach { widget ->
                //attachWidget(Intent.parseUri(widget, 0))
                getWidgetView(Intent.parseUri(widget, 0))?.let {
                    widgets.add(it)
                }
            }
            withContext(Dispatchers.Main) {
                widgetsAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun setUpWallpaper() =
            binding.mWallpaper.setImageDrawable(WallpaperManager.getInstance(applicationContext).drawable)

    override fun onStart() {
        super.onStart()
        widgetHost.startListening()
    }

    override fun onStop() {
        super.onStop()
        widgetHost.stopListening()
    }

    private fun getWidgetView(data: Intent): AppWidgetHostView? {
        val extras = data.extras
        val id = extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
        val info = widgetManager.getAppWidgetInfo(id)
        return widgetHost.createView(applicationContext, id, info)?.apply {
            setAppWidget(id, info)
        }
    }

    private fun attachWidget(data: Intent) {
        getWidgetView(data)?.let {
            widgets.add(it)
        }
        widgetsAdapter.notifyDataSetChanged()
    }

    override fun onRemoveWidget(widget: AppWidgetHostView) {
        val id = widget.appWidgetId
        widgetHost.deleteAppWidgetId(id)
        widgets.remove(widget)
        widgetsAdapter.notifyDataSetChanged()

        launch {
            val widgets = preferences.getStringSet("widgets", setOf())
                    ?.filter {
                        Intent.parseUri(it, 0)?.extras
                                ?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1 != id
                    }?.toSet()

            preferences.edit(commit = true) {
                putStringSet("widgets", widgets)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (resultCode) {
            RESULT_OK -> {
                when (requestCode) {
                    ADD_WIDGET -> {
                        val extras = data?.extras
                        val id = extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
                        val info = widgetManager.getAppWidgetInfo(id)
                        if (info?.configure != null) {
                            startActivityForResult(Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                                component = info.configure
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                            }, ATTACH_WIDGET)
                        } else data?.let { attachWidget(it) }

                        GlobalScope.launch {
                            val widgets = preferences.getStringSet("widgets", setOf())
                            preferences.edit(commit = true) {
                                putStringSet("widgets", widgets?.plus(data!!.toUri(0)))
                            }
                        }
                    }
                    ATTACH_WIDGET -> data?.let { attachWidget(it) }
                    CHANGE_WALLPAPER -> setUpWallpaper()
                }
            }
            RESULT_CANCELED -> data?.let {
                val extras = it.extras
                val id = extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
                if (id != -1) {
                    widgetHost.deleteAppWidgetId(id)
                }
            }
        }
    }

    override fun onActionSelected(index: Int) {
        when (index) {
            0 -> {
                val id = widgetHost.allocateAppWidgetId()
                startActivityForResult(Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                    putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, arrayListOf())
                    putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, arrayListOf())
                }, ADD_WIDGET)
            }
            1 -> {
                startActivityForResult(Intent.createChooser(Intent(Intent.ACTION_SET_WALLPAPER),
                        getString(R.string.change_wallpaper)), CHANGE_WALLPAPER)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSIONS) {
            permissions.forEachIndexed { index, permission ->
                when (permission) {
                    READ_EXTERNAL_STORAGE -> {
                        if (grantResults[index] == PERMISSION_GRANTED)
                            setUpWallpaper()
                    }
                }
            }
        }
    }

    companion object {
        private val HOST_ID = Random.nextInt()
        private const val REQUEST_PERMISSIONS = 3
        const val CHANGE_WALLPAPER = 4
        const val ADD_WIDGET = 0
        private const val ATTACH_WIDGET = 1
    }
}