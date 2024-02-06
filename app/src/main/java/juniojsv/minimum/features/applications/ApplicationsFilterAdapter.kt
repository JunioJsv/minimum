package juniojsv.minimum.features.applications

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import juniojsv.minimum.R
import juniojsv.minimum.databinding.ApplicationsFilterBinding

class ApplicationsFilterAdapter(private val listener: SearchView.OnQueryTextListener) :
    RecyclerView.Adapter<ApplicationsFilterViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ApplicationsFilterViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ApplicationsFilterBinding.inflate(layoutInflater)

        return ApplicationsFilterViewHolder(binding, listener)
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.applications_filter
    }

    override fun getItemId(position: Int): Long {
        return getItemViewType(position).toLong()
    }

    override fun getItemCount(): Int = 1

    override fun onBindViewHolder(holder: ApplicationsFilterViewHolder, position: Int) {
        holder.bind()
    }
}