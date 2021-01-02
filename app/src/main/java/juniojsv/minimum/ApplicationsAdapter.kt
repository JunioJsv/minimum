package juniojsv.minimum

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import juniojsv.minimum.databinding.ApplicationGridVariantBinding
import juniojsv.minimum.databinding.ApplicationListVariantBinding
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

    abstract class ApplicationHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(application: Application, index: Int, onHolderClick: OnHolderClick)
    }

    private class GridVariant(private val binding: ApplicationGridVariantBinding) : ApplicationHolder(binding.root) {
        override fun bind(application: Application, index: Int, onHolderClick: OnHolderClick) {
            with(binding) {
                mLabel.text = application.label
                mIncludeApplicationIcon.apply {
                    mIcon.setImageBitmap(application.icon)
                    mNew.visibility = if (application.isNew) View.VISIBLE else View.GONE
                    mFavorite.visibility = if (application.isFavorite) View.VISIBLE else View.GONE
                }
                root.setOnClickListener {
                    onHolderClick.onClick(application, it, index)
                }

                root.setOnLongClickListener {
                    onHolderClick.onLongClick(application, it, index)
                    true
                }
            }
        }
    }

    private class ListVariant(private val binding: ApplicationListVariantBinding) : ApplicationHolder(binding.root) {
        override fun bind(application: Application, index: Int, onHolderClick: OnHolderClick) {
            with(binding) {
                mLabel.text = application.label
                mIncludeApplicationIcon.apply {
                    mIcon.setImageBitmap(application.icon)
                    mNew.visibility = if (application.isNew) View.VISIBLE else View.GONE
                    mFavorite.visibility = if (application.isFavorite) View.VISIBLE else View.GONE
                }
                root.setOnClickListener {
                    onHolderClick.onClick(application, it, index)
                }

                root.setOnLongClickListener {
                    onHolderClick.onLongClick(application, it, index)
                    true
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationHolder {
        return when ((parent as RecyclerView).layoutManager) {
            is GridLayoutManager -> GridVariant(ApplicationGridVariantBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false))
            else -> ListVariant(ApplicationListVariantBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false))
        }
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
