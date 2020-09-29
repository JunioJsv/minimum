package juniojsv.minimum

import android.annotation.SuppressLint
import android.appwidget.AppWidgetHostView
import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.appcompat.widget.PopupMenu

@SuppressLint("ViewConstructor")
class WidgetContainer(context: Context, val widget: AppWidgetHostView, private val listener: Listener) : FrameLayout(context), PopupMenu.OnMenuItemClickListener {

    interface Listener {
        fun onRemove(container: WidgetContainer)
    }

    init {
        background = resources.getDrawable(R.drawable.widget_background, null)
        addView(widget.apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                val value = 8.toDpi(context)
                marginStart = value
                marginEnd = value
            }
        })
        addView(LayoutInflater.from(context)
                .inflate(R.layout.widget_actions_button, this, false).also { view ->
                    view.setOnClickListener {
                        PopupMenu(context, view).also { popup ->
                            popup.menu.apply {
                                add(0, REMOVE_WIDGET, 0, resources.getString(R.string.remove))
                            }
                            popup.setOnMenuItemClickListener(this)
                        }.show()
                    }
                })

    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            REMOVE_WIDGET -> listener.onRemove(this)
        }
        return true
    }

    companion object {
        private const val REMOVE_WIDGET = 0
    }
}