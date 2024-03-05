package juniojsv.minimum.features.applications

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.AsyncListDiffer.ListListener
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import juniojsv.minimum.databinding.ApplicationGridVariantBinding
import juniojsv.minimum.models.Application

class ApplicationsGroupAdapter(
    val callbacks: ApplicationsAdapter.Callbacks,
    private val onChangeApplication: (application: Application) -> Unit
) : RecyclerView.Adapter<ApplicationBaseViewHolder>() {
    private val differ = AsyncListDiffer(this, object : DiffUtil.ItemCallback<Application>() {
        override fun areItemsTheSame(oldItem: Application, newItem: Application): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: Application, newItem: Application): Boolean {
            return oldItem == newItem
        }
    })

    fun addListListener(listener: ListListener<Application>) {
        differ.addListListener(listener)
    }

    fun removeListListener(listener: ListListener<Application>) {
        differ.removeListListener(listener)
    }


    init {
        setHasStableIds(true)
    }

    fun setApplications(applications: List<Application>) {
        differ.submitList(applications.sorted())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationBaseViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        val binding = ApplicationGridVariantBinding
            .inflate(
                layoutInflater,
                parent,
                false
            )

        return ApplicationViewHolder(
            binding,
            callbacks,
            onChangeApplication,
            true
        )
    }

    override fun getItemId(position: Int): Long {
        return differ.currentList[position].hashCode().toLong()
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ApplicationBaseViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }
}