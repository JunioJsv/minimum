package juniojsv.minimum

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.application_icon.view.*
import kotlinx.android.synthetic.main.application_list_variant.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ApplicationsAdapter(private val applications: ArrayList<Application>, private val onHolderClick: OnHolderClick) : RecyclerView.Adapter<ApplicationsAdapter.ApplicationHolder>() {
    private val showOnly = arrayListOf<Int>()
    private var showOnlyBookmarks = false
    private var filterQuery: String = String()

    interface OnHolderClick {
        fun onClick(application: Application, view: View, position: Int)
        fun onLongClick(application: Application, view: View, position: Int)
    }

    class ApplicationHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(application: Application, index: Int, onHolderClick: OnHolderClick) {
            with(view) {
                mLabel.text = application.label
                mIcon.setImageBitmap(application.icon)
                mNew.visibility = if (application.isNew) View.VISIBLE else View.GONE
                mFavorite.visibility = if (application.isFavorite) View.VISIBLE else View.GONE

                setOnClickListener {
                    onHolderClick.onClick(application, this, index)
                }

                setOnLongClickListener {
                    onHolderClick.onLongClick(application, this, index)
                    true
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationHolder {
        return ApplicationHolder(LayoutInflater.from(parent.context)
                .inflate(if ((parent as RecyclerView).layoutManager is GridLayoutManager)
                    R.layout.application_grid_variant
                else R.layout.application_list_variant, parent, false))
    }

    override fun getItemCount(): Int =
            if (showOnly.isEmpty() && filterQuery.isEmpty()) applications.size else showOnly.size

    override fun onBindViewHolder(holder: ApplicationHolder, position: Int) {
        val positionHandler = if (showOnly.isNotEmpty()) showOnly[position] else position
        holder.bind(applications[positionHandler], position, onHolderClick)
    }

    fun filterViews() = GlobalScope.launch {
        showOnly.clear()

        if (showOnlyBookmarks && filterQuery.isEmpty()) {
            applications.forEachIndexed { index, application ->
                if (application.isFavorite)
                    showOnly.add(index)
            }
        } else if (showOnlyBookmarks && filterQuery.isNotEmpty()) {
            applications.forEachIndexed { index, application ->
                if (application.label.contains(filterQuery, true) && application.isFavorite)
                    showOnly.add(index)
            }
        } else if (filterQuery.isNotEmpty()) {
            applications.forEachIndexed { index, application ->
                if (application.label.contains(filterQuery, true))
                    showOnly.add(index)
            }
        }

        withContext(Dispatchers.Main) {
            notifyDataSetChanged()
        }
    }

    fun setShowOnlyBookmarks(value: Boolean) {
        showOnlyBookmarks = value
    }

    fun setFilterQuery(value: String) {
        filterQuery = value
    }

}
