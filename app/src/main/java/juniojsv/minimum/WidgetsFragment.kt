package juniojsv.minimum

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.WallpaperManager
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.widgets_fragment.*

class WidgetsFragment : Fragment(), DialogInterface.OnClickListener, WidgetContainer.Listener {
    private lateinit var widgets: AppWidgetManager
    private lateinit var widgetHost: AppWidgetHost
    private lateinit var applicationContext: Context
    private val actions = WidgetsActionsDialog(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.widgets_fragment, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        applicationContext = requireContext().applicationContext

        if (SDK_INT >= 26 && ActivityCompat.checkSelfPermission(applicationContext, READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            requestPermissions(arrayOf(READ_EXTERNAL_STORAGE), REQUEST_PERMISSIONS)
        } else {
            setUpWallpaper()
        }

        mEditActions.setOnClickListener {
            actions.show(parentFragmentManager, "WidgetsActions")
        }

        mWidgets.setContainerScrollView(mScrollWidgets)

        widgets = AppWidgetManager.getInstance(applicationContext)
        widgetHost = AppWidgetHost(applicationContext, HOST_ID)
    }

    private fun setUpWallpaper() =
            mWallpaper.setImageDrawable(WallpaperManager.getInstance(applicationContext).drawable)

    override fun onStart() {
        super.onStart()
        widgetHost.startListening()
    }

    override fun onStop() {
        super.onStop()
        widgetHost.stopListening()
    }

    private fun selectWidget() {
        val id = widgetHost.allocateAppWidgetId()
        startActivityForResult(Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
            putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, arrayListOf())
            putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, arrayListOf())
        }, SELECT_WIDGET)
    }

    private fun attachWidget(data: Intent) {
        val extras = data.extras
        val id = extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
        val info = widgets.getAppWidgetInfo(id)
        val widget = widgetHost.createView(applicationContext, id, info)?.apply {
            setAppWidget(id, info)
        }

        if (widget != null) {
            val container = WidgetContainer(requireContext(), widget, this)
            mWidgets.addDragView(container, container)
        }
    }

    override fun onClick(dialog: DialogInterface?, index: Int) {
        when (index) {
            0 -> selectWidget()
            1 -> startActivityForResult(Intent.createChooser(Intent(Intent.ACTION_SET_WALLPAPER),
                    getString(R.string.change_wallpaper)), CHANGE_WALLPAPER)
        }
    }

    override fun onRemove(container: WidgetContainer) {
        widgetHost.deleteAppWidgetId(container.widget.appWidgetId)
        mWidgets.removeDragView(container)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (resultCode) {
            RESULT_OK -> {
                when (requestCode) {
                    SELECT_WIDGET -> {
                        val extras = data?.extras
                        val id = extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
                        val info = widgets.getAppWidgetInfo(id)
                        if (info?.configure != null) {
                            startActivityForResult(Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                                component = info.configure
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                            }, ATTACH_WIDGET)
                        } else attachWidget(data!!)
                    }
                    ATTACH_WIDGET -> {
                        attachWidget(data!!)
                    }
                    CHANGE_WALLPAPER -> setUpWallpaper()
                }
            }
            RESULT_CANCELED -> {
                if (data != null) {
                    val extras = data.extras
                    val id = extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
                    if (id != -1) {
                        widgetHost.deleteAppWidgetId(id)
                    }
                }
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
        private val HOST_ID = "juniojsv.minimum.AppWidgetHost.id".hashCode()
        private const val REQUEST_PERMISSIONS = 3
        private const val CHANGE_WALLPAPER = 4
        private const val SELECT_WIDGET = 0
        private const val ATTACH_WIDGET = 1
    }
}