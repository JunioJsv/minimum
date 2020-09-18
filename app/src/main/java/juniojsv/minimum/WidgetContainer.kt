package juniojsv.minimum

import android.annotation.SuppressLint
import android.appwidget.AppWidgetHostView
import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout

@SuppressLint("ViewConstructor")
class WidgetContainer(context: Context, val widget: AppWidgetHostView, listener: Listener) : FrameLayout(context) {

    interface Listener {
        fun onRemove(container: WidgetContainer)
    }

    init {
        addView(widget.apply {
            background = resources.getDrawable(R.drawable.widget_background, null)
        })
        addView(LayoutInflater.from(context)
                .inflate(R.layout.widget_remove_button, this, false).also { view ->
                    view.setOnClickListener { listener.onRemove(this) }
                })

    }
}