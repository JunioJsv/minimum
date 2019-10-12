package juniojsv.minimum

import android.content.Context
import android.view.View
import android.view.View.inflate
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.app_view.view.*

class Adapter(private val context: Context, private var apps: ArrayList<App>) : BaseAdapter() {

    override fun getCount(): Int {
        return apps.size
    }

    override fun getItem(position: Int): App {
        return apps[position]
    }

    override fun getItemId(position: Int): Long {
        return apps.indexOf(apps[position]).toLong()
    }

    fun changeList(apps: ArrayList<App>) {
        this.apps = apps
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {

        return inflate(context, R.layout.app_view, null).apply {
            icon_view.setImageDrawable(apps[position].icon)
            name_view.text = apps[position].packageLabel
        }
    }
}
