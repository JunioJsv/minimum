package juniojsv.minimum

import android.content.Context
import android.view.View
import android.view.View.inflate
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class Adapter internal constructor(private val context: Context, private val appsList: MutableList<App>) : BaseAdapter() {

    override fun getCount(): Int {
        return appsList.size
    }

    override fun getItem(position: Int): App {
        return appsList[position]
    }

    override fun getItemId(position: Int): Long {
        return appsList.indexOf(appsList[position]).toLong()
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val targetView: View = inflate(context, R.layout.app_view, null)

        val iconView: ImageView = targetView.findViewById(R.id.icon_view)
        val nameView: TextView = targetView.findViewById(R.id.name_view)

        iconView.setImageDrawable(appsList[position].icon)
        nameView.text = appsList[position].packageLabel
        return targetView
    }
}
