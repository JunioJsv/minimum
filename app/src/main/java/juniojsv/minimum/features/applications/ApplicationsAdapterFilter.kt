package juniojsv.minimum.features.applications

import androidx.appcompat.widget.SearchView
import juniojsv.minimum.models.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class ApplicationsAdapterFilter(
    private val applications: ArrayList<Application>,
    private val callbacks: Callbacks
) : SearchView.OnQueryTextListener, CoroutineScope {
    private var lastQuery = String()

    val isFiltering = lastQuery.isEmpty()

    interface Callbacks {
        fun onShowOnlyApplicationsWithIndexChange(indexes: List<Int>)
        fun onStopFilteringApplications()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + Job()

    private var debounce: Job? = null

    private suspend fun byLabel(label: String) = coroutineScope {
        val indexes = applications.mapIndexed { index, application ->
            async {
                if (application.label.contains(
                        label,
                        true
                    )
                ) index else null
            }
        }.awaitAll().filterNotNull()
        withContext(Dispatchers.Main) {
            callbacks.onShowOnlyApplicationsWithIndexChange(indexes)
        }
    }

    suspend fun byLastQuery() = byLabel(lastQuery)

    override fun onQueryTextSubmit(query: String): Boolean {
        debounce?.cancel()
        debounce = launch {
            delay(DEBOUNCE_DELAY)
            if (query.isBlank()) {
                withContext(Dispatchers.Main) {
                    callbacks.onStopFilteringApplications()
                }
            } else if (query != lastQuery) {
                byLabel(query)
            }
            lastQuery = query
        }
        return true
    }

    override fun onQueryTextChange(query: String): Boolean = onQueryTextSubmit(query)

    companion object {
        const val DEBOUNCE_DELAY = 250L
    }
}