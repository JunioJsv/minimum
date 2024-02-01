package juniojsv.minimum.features.applications

import androidx.appcompat.widget.SearchView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class ApplicationsAdapterSearch(
    private val applicationsAdapter: ApplicationsAdapter,
    private val applications: ArrayList<Application>
) : SearchView.OnQueryTextListener, CoroutineScope {
    val showOnly = arrayListOf<Int>()
    private var lastQuery = String()

    val isSeeking: Boolean get() = showOnly.isNotEmpty()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + Job()

    private var debounceSearchJob: Job? = null

    private fun applyFilter(query: String) {
        showOnly.clear()
        applications.forEachIndexed { index, application ->
            if (application.label.contains(query, true))
                showOnly.add(index)
        }
        if (showOnly.isEmpty())
            showOnly.add(NOT_FOUND)
        if (showOnly.size == applications.size)
            showOnly.clear()
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        debounceSearchJob?.cancel()
        debounceSearchJob = launch {
            delay(DEBOUNCE_DELAY)
            if (query != lastQuery) {
                applyFilter(query)
                withContext(Dispatchers.Main) {
                    applicationsAdapter.notifyDataSetChanged()
                }
            }
            lastQuery = query
        }
        return true
    }

    fun notifyDataSetChanged() = launch {
        applyFilter(lastQuery)
        withContext(Dispatchers.Main) {
            applicationsAdapter.notifyDataSetChanged()
        }
    }

    override fun onQueryTextChange(newText: String): Boolean = onQueryTextSubmit(newText)

    companion object {
        const val DEBOUNCE_DELAY = 250L
        const val NOT_FOUND = -1
    }
}