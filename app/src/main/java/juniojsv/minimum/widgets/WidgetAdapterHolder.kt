package juniojsv.minimum.widgets

import android.appwidget.AppWidgetHostView
import android.view.ViewGroup.LayoutParams
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import juniojsv.minimum.R
import juniojsv.minimum.databinding.WidgetBinding

class WidgetAdapterHolder(private val binding: WidgetBinding, private val holderListener: HolderListener) : RecyclerView.ViewHolder(binding.root) {

    interface HolderListener {
        fun onRemoveWidget(widget: AppWidgetHostView)
    }

    fun bind(widget: AppWidgetHostView) {
        binding.mWidget.apply {
            removeAllViews()
            addView(widget, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        }
        binding.mWidgetActions.setOnClickListener {
            PopupMenu(widget.context, it).also { popup ->
                popup.menu.apply {
                    add(0, 0, 0, widget.context.getString(R.string.remove))
                }
                popup.setOnMenuItemClickListener { item ->
                    when(item.itemId) {
                        0 -> holderListener.onRemoveWidget(widget)
                    }
                    return@setOnMenuItemClickListener true
                }
            }.show()
        }
    }
}