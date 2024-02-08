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
import kotlin.coroutines.CoroutineContext

class ApplicationsFilter(
    private val applications: ArrayList<Application>,
    private val callbacks: Callbacks
) : SearchView.OnQueryTextListener, CoroutineScope {
    private var lastQuery = String()

    val isFiltering get() = lastQuery.isNotEmpty()

    interface Callbacks {
        suspend fun onShowOnlyApplicationsWithIndex(indexes: List<Int>)
        suspend fun onStopFilteringApplications()
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
        callbacks.onShowOnlyApplicationsWithIndex(indexes)
    }

    suspend fun byLastQuery() = byLabel(lastQuery)

    override fun onQueryTextSubmit(query: String): Boolean {
        debounce?.cancel()
        if (query != lastQuery) {
            debounce = launch {
                delay(DEBOUNCE_DELAY)
                if (query.isEmpty()) {
                    callbacks.onStopFilteringApplications()
                } else {
                    byLabel(query)
                }
                lastQuery = query
            }
        }
        return true
    }

    override fun onQueryTextChange(query: String): Boolean = onQueryTextSubmit(query)

    companion object {
        const val DEBOUNCE_DELAY = 250L
    }
}