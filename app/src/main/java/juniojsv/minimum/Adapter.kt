package juniojsv.minimum

import android.content.Context
import android.view.View
import android.view.View.inflate
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.SectionIndexer
import kotlinx.android.synthetic.main.app_view.view.*

class Adapter(private val context: Context, private var apps: ArrayList<App>) : BaseAdapter(), SectionIndexer {

    override fun getSections(): Array<Char> = Array(apps.size) { index ->
        apps[index].label[0].toUpperCase()
    }.toSet().toTypedArray()

    override fun getSectionForPosition(position: Int): Int {
        return sections.find { char ->
            char == apps[position].label[0].toUpperCase()
        }?.let { sections.indexOf(it) } ?: return 0
    }

    override fun getPositionForSection(sectionIndex: Int): Int {
        return apps.find { app ->
            app.label[0].toUpperCase() == sections[sectionIndex]
        }?.let { apps.indexOf(it) } ?: return 0
    }

    override fun getCount(): Int = apps.size

    override fun getItem(position: Int): App = apps[position]

    override fun getItemId(position: Int): Long = apps.indexOf(apps[position]).toLong()

    fun changeList(apps: ArrayList<App>) {
        this.apps = apps
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View =
            inflate(context, R.layout.app_view, null).apply {
                icon_view.setImageDrawable(apps[position].icon)
                label_view.text = apps[position].label
                tag_new.visibility = if (apps[position].isNew) View.VISIBLE else View.GONE
            }
}
