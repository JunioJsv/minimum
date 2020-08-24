package juniojsv.minimum

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.application.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ApplicationsAdapter(private val applications: ArrayList<Application>, private val onHolderClick: OnHolderClick) : RecyclerView.Adapter<ApplicationsAdapter.ApplicationHolder>() {
    private val showOnly = arrayListOf<Int>()
    private var gridView = false
    private var showOnlyFiltered = false
    private var showOnlyBookmarks = false

    interface OnHolderClick {
        fun onClick(application: Application, adapter: ApplicationsAdapter, position: Int)
        fun onLongClick(application: Application, position: Int)
    }

    class ApplicationHolder(private val view: View, private val adapter: ApplicationsAdapter) : RecyclerView.ViewHolder(view) {

        fun bind(application: Application, index: Int, onHolderClick: OnHolderClick) {
            with(view) {
                mLabel.text = application.label
                mIcon.setImageBitmap(application.icon)
                mNew.visibility = if (application.isNew) View.VISIBLE else View.GONE
                mFavorite.visibility = if (application.isFavorite) View.VISIBLE else View.GONE

                setOnClickListener {
                    onHolderClick.onClick(application, adapter, index)
                }
                setOnLongClickListener {
                    onHolderClick.onLongClick(application, index)
                    true
                }
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        if (recyclerView.layoutManager is GridLayoutManager) gridView = true
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationHolder {
        return ApplicationHolder(
                LayoutInflater
                        .from(parent.context).inflate(
                                if (gridView) R.layout.application_grid_variant
                                else R.layout.application, parent, false), this)
    }

    override fun getItemCount(): Int =
            if (showOnly.isEmpty() && !showOnlyFiltered) applications.size else showOnly.size

    override fun onBindViewHolder(holder: ApplicationHolder, position: Int) {
        val positionHandler = if (showOnly.isNotEmpty()) showOnly[position] else position
        holder.bind(applications[positionHandler], position, onHolderClick)
    }

    fun filterViews(string: String? = null, onFinished: (showOnly: ArrayList<Int>) -> Unit) = GlobalScope.launch {
        showOnly.clear()
        if (showOnlyBookmarks && string.isNullOrEmpty()) {
            showOnlyFiltered = true
            applications.forEachIndexed { index, application ->
                if (application.isFavorite)
                    showOnly.add(index)
            }
        }
        else if (showOnlyBookmarks && !string.isNullOrEmpty()) {
            showOnlyFiltered = true
            applications.forEachIndexed { index, application ->
                if (application.label.contains(string, true) && application.isFavorite)
                    showOnly.add(index)
            }
        }
        else if (!string.isNullOrEmpty()) {
            showOnlyFiltered = true
            applications.forEachIndexed { index, application ->
                if (application.label.contains(string, true))
                    showOnly.add(index)
            }
        } else
            showOnlyFiltered = false
        onFinished(showOnly)
    }

    fun setShowOnlyBookmarks(value: Boolean) {
        showOnlyBookmarks = value
    }
}
