package juniojsv.minimum

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import juniojsv.minimum.databinding.ApplicationGridVariantBinding
import juniojsv.minimum.databinding.ApplicationListVariantBinding

abstract class ApplicationHolder(view: View) : RecyclerView.ViewHolder(view) {
    interface OnHolderClick {
        fun onClick(application: Application, view: View, position: Int)
        fun onLongClick(application: Application, view: View, position: Int)
    }

    abstract fun bind(application: Application, index: Int, onHolderClick: OnHolderClick)
}

private class GridVariant(private val binding: ApplicationGridVariantBinding) : ApplicationHolder(binding.root) {
    override fun bind(application: Application, index: Int, onHolderClick: OnHolderClick) {
        with(binding) {
            mLabel.text = application.label
            mIncludeApplicationIcon.apply {
                mIcon.setImageBitmap(application.icon)
                mNew.visibility = if (application.isNew) View.VISIBLE else View.GONE
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

class ApplicationsAdapter(private val applications: ArrayList<Application>, private val onHolderClick: ApplicationHolder.OnHolderClick) : RecyclerView.Adapter<ApplicationHolder>() {
    val searchHandler = ApplicationsAdapterSearch(this, applications)

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationHolder {
        return when ((parent as RecyclerView).layoutManager) {
            is GridLayoutManager -> GridVariant(ApplicationGridVariantBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false))
            else -> ListVariant(ApplicationListVariantBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun getItemCount(): Int = when {
        searchHandler.showOnly.isNotEmpty()
                && searchHandler.showOnly[0] == ApplicationsAdapterSearch.NOT_FOUND -> 0
        searchHandler.showOnly.isNotEmpty() -> searchHandler.showOnly.size
        else -> applications.size
    }

    override fun getItemId(position: Int): Long {

        val positionHandler = if (searchHandler.showOnly.isNotEmpty())
            searchHandler.showOnly[position] else position

        return applications[positionHandler].hashCode().toLong()
    }

    override fun onBindViewHolder(holder: ApplicationHolder, position: Int) {

        val positionHandler = if (searchHandler.showOnly.isNotEmpty())
            searchHandler.showOnly[position] else position

        holder.bind(applications[positionHandler], position, onHolderClick)
    }
}
