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

class ApplicationsAdapter(private val applications: ArrayList<Application>, private val onHolderClick: ApplicationHolder.OnHolderClick) : RecyclerView.Adapter<ApplicationHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationHolder {
        return when ((parent as RecyclerView).layoutManager) {
            is GridLayoutManager -> GridVariant(ApplicationGridVariantBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false))
            else -> ListVariant(ApplicationListVariantBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun getItemCount(): Int = applications.size

    override fun onBindViewHolder(holder: ApplicationHolder, position: Int) {
        holder.bind(applications[position], position, onHolderClick)
    }
}
