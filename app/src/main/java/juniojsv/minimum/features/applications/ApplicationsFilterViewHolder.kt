package juniojsv.minimum.features.applications

import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import juniojsv.minimum.databinding.ApplicationsFilterBinding

class ApplicationsFilterViewHolder(
    private val binding: ApplicationsFilterBinding,
    private val listener: SearchView.OnQueryTextListener,
    private val divider: Boolean
) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind() {
        with(binding.applicationsFilterButton) {
            maxWidth = Int.MAX_VALUE
            setOnQueryTextListener(listener)
        }
        binding.applicationsFilterDivider.visibility = if (divider) View.VISIBLE else View.GONE
    }
}