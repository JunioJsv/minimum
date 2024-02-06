package juniojsv.minimum.features.applications

import android.animation.LayoutTransition
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import juniojsv.minimum.databinding.ApplicationsFilterBinding

class ApplicationsFilterViewHolder(
    private val binding: ApplicationsFilterBinding,
    private val listener: SearchView.OnQueryTextListener
) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind() {
        with(binding.applicationsFilterButton) {
            layoutTransition = LayoutTransition()
            maxWidth = Int.MAX_VALUE
            setOnQueryTextListener(listener)
            setOnSearchClickListener {
                binding.applicationsFilterTitle.visibility = GONE
            }
            setOnCloseListener {
                binding.applicationsFilterTitle.visibility = VISIBLE
                false
            }
        }
    }
}