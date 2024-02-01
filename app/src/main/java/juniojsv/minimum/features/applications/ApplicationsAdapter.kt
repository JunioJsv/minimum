package juniojsv.minimum.features.applications

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import juniojsv.minimum.databinding.ApplicationGridVariantBinding
import juniojsv.minimum.databinding.ApplicationListVariantBinding

class ApplicationsAdapter(
    private val applications: ArrayList<Application>,
    private val holderListener: ApplicationAdapterHolder.HolderListener
) : RecyclerView.Adapter<ApplicationAdapterHolder>() {
    val searchHandler = ApplicationsAdapterSearch(this, applications)

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationAdapterHolder {
        return when ((parent as RecyclerView).layoutManager) {
            is GridLayoutManager -> ApplicationAdapterHolder(
                ApplicationGridVariantBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
            )

            else -> ApplicationAdapterHolder(
                ApplicationListVariantBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
            )
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

    override fun onBindViewHolder(holder: ApplicationAdapterHolder, position: Int) {

        val positionHandler = if (searchHandler.showOnly.isNotEmpty())
            searchHandler.showOnly[position] else position

        holder.bind(applications[positionHandler], position, holderListener)
    }
}
