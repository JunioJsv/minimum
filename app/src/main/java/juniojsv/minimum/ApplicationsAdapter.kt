package juniojsv.minimum

import android.content.Context
import android.view.View
import android.view.View.inflate
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.SectionIndexer
import kotlinx.android.synthetic.main.application_view.view.*

class ApplicationsAdapter(private val context: Context, private var applications: ArrayList<Application>) : BaseAdapter(), SectionIndexer {

    override fun getSections(): Array<Char> = Array(applications.size) { index ->
        applications[index].label[0].toUpperCase()
    }.toSet().toTypedArray()

    override fun getSectionForPosition(position: Int): Int {
        return sections.find { char ->
            char == applications[position].label[0].toUpperCase()
        }?.let { sections.indexOf(it) } ?: return 0
    }

    override fun getPositionForSection(sectionIndex: Int): Int {
        return applications.find { application ->
            application.label[0].toUpperCase() == sections[sectionIndex]
        }?.let { applications.indexOf(it) } ?: return 0
    }

    override fun getCount(): Int = applications.size

    override fun getItem(position: Int): Application = applications[position]

    override fun getItemId(position: Int): Long = applications.indexOf(applications[position]).toLong()

    fun changeList(applications: ArrayList<Application>) {
        this.applications = applications
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View =
            inflate(context, R.layout.application_view, null).apply {
                icon_view.setImageDrawable(applications[position].icon)
                label_view.text = applications[position].label
                newly_installed_view.visibility = if (applications[position].newlyInstalled) View.VISIBLE else View.GONE
            }
}
