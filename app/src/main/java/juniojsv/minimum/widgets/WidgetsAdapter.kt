package juniojsv.minimum.widgets

import android.appwidget.AppWidgetHostView
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import juniojsv.minimum.databinding.WidgetBinding

class WidgetsAdapter(private val widgets: ArrayList<AppWidgetHostView>, private val holderListener: WidgetAdapterHolder.HolderListener) : RecyclerView.Adapter<WidgetAdapterHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WidgetAdapterHolder {
        return WidgetAdapterHolder(WidgetBinding.inflate(LayoutInflater.from(parent.context)), holderListener)
    }

    override fun onBindViewHolder(holder: WidgetAdapterHolder, position: Int) {
        holder.bind(widgets[position])
    }

    override fun getItemCount(): Int = widgets.size

    override fun getItemId(position: Int): Long {
        return widgets[position].hashCode().toLong()
    }
}