package juniojsv.minimum.features.applications

import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import juniojsv.minimum.models.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class ApplicationsAdapterController(adapter: ApplicationsAdapter) :
    DiffUtil.ItemCallback<Application>(), ApplicationsFilter.Callbacks, CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + Job()
    private val applications = arrayListOf<Application>()
    private val differ = AsyncListDiffer(adapter, this)
    val filter = ApplicationsFilter(applications, this)

    suspend fun setInstalledApplications(applications: List<Application>) {
        this.applications.apply {
            clear()
            addAll(applications)
        }
        onInstalledApplicationsChanged()
    }

    suspend fun addInstalledApplication(application: Application) {
        applications.add(application)
        onInstalledApplicationsChanged()
    }

    suspend fun removeInstalledApplicationAt(index: Int) {
        applications.removeAt(index)
        onInstalledApplicationsChanged()
    }

    suspend fun setInstalledApplicationAt(index: Int, application: Application) {
        applications[index] = application
        onInstalledApplicationsChanged()
    }

    private suspend fun setAdapterApplicationsByLastFilterQuery() = filter.byLastQuery()

    private suspend fun onInstalledApplicationsChanged() {
        if (filter.isFiltering) {
            setAdapterApplicationsByLastFilterQuery()
        } else {
            setAdapterApplications { applications }
        }
    }

    fun getInstalledApplicationIndexByPackageName(packageName: String) =
        applications.indexOfFirst { it.packageName == packageName }

    private suspend fun setAdapterApplications(
        callback: (applications: List<Application>) -> List<Application>
    ) = coroutineScope {
        val update = callback(differ.currentList).sorted()
        withContext(Dispatchers.Main) {
            differ.submitList(update)
        }
    }

    fun getPinnedApplicationsPackages() =
        applications.mapNotNull { application ->
            var packageName: String? = null
            if (application.isPinned) {
                packageName = application.packageName
            }

            packageName
        }

    fun getInstalledApplicationAt(index: Int): Application = applications[index]
    fun getAdapterApplicationAt(position: Int): Application = differ.currentList[position]

    override fun areItemsTheSame(oldItem: Application, newItem: Application): Boolean {
        return oldItem.packageName == newItem.packageName
    }

    override fun areContentsTheSame(oldItem: Application, newItem: Application): Boolean {
        return oldItem == newItem
    }

    fun getInstalledApplicationsCount() = applications.size

    fun getAdapterApplicationsCount(): Int = differ.currentList.size

    fun getAdapterApplicationId(position: Int): Long {
        return differ.currentList[position].hashCode().toLong()
    }

    override suspend fun onShowOnlyApplicationsWithIndex(indexes: List<Int>) {
        setAdapterApplications {
            indexes.map { index -> applications[index] }
        }
    }

    override suspend fun onStopFilteringApplications() {
        setAdapterApplications { applications }
    }
}