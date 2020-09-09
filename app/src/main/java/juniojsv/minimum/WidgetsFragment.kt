package juniojsv.minimum

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.WallpaperManager
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.widgets_fragment.*

class WidgetsFragment : Fragment() {
    private lateinit var widgets: AppWidgetManager
    private lateinit var widgetHost: AppWidgetHost

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.widgets_fragment, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mWallpaper.setImageDrawable(WallpaperManager.getInstance(requireContext()).drawable)

        mEditActions.setOnClickListener {
            selectWidget()
        }

        mWidgets.setContainerScrollView(mScrollWidgets)

        widgets = AppWidgetManager.getInstance(requireContext().applicationContext)
        widgetHost = AppWidgetHost(requireContext().applicationContext, HOST_ID)
    }

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
        val widget = widgetHost.createView(requireContext().applicationContext, id, info)?.apply {
            setAppWidget(id, info)
        }

        widget?.apply {
            background = resources.getDrawable(R.drawable.widget_background, null)
        }

        mWidgets.addDragView(widget, widget)
    }

    private fun deAttachWidget(widget: AppWidgetHostView) {
        widgetHost.deleteAppWidgetId(widget.appWidgetId)
        mWidgets.removeDragView(widget)
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

    companion object {
        private val HOST_ID = "juniojsv.minimum.AppWidgetHost.id".hashCode()
        private const val SELECT_WIDGET = 0
        private const val ATTACH_WIDGET = 1
    }
}