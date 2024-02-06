package juniojsv.minimum.features.applications

import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import juniojsv.minimum.models.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class ApplicationsAdapterController(adapter: ApplicationsAdapter) :
    DiffUtil.ItemCallback<Application>(), ApplicationsAdapterFilter.Callbacks, CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + Job()
    private val applications = arrayListOf<Application>()
    private val differ = AsyncListDiffer(adapter, this)
    val filter = ApplicationsAdapterFilter(applications, this)

    fun setInstalledApplications(applications: List<Application>) {
        this.applications.apply {
            clear()
            addAll(applications)
        }
        onInstalledApplicationsChanged()
    }

    fun addInstalledApplication(application: Application) {
        applications.add(application)
        onInstalledApplicationsChanged()
    }

    fun removeInstalledApplicationAt(index: Int) {
        applications.removeAt(index)
        onInstalledApplicationsChanged()
    }

    fun setInstalledApplicationAt(index: Int, application: Application) {
        applications[index] = application
        onInstalledApplicationsChanged()
    }

    private fun setAdapterApplicationsByLastFilterQuery() = launch {
        filter.byLastQuery()
    }

    private fun onInstalledApplicationsChanged() {
        if (filter.isFiltering) {
            setAdapterApplicationsByLastFilterQuery()
        } else {
            setAdapterApplications { applications }
        }
    }

    fun getInstalledApplicationIndexByPackageName(packageName: String) =
        applications.indexOfFirst { it.packageName == packageName }

    private fun setAdapterApplications(
        callback: (applications: List<Application>) -> List<Application>
    ) {
        launch {
            val update = callback(differ.currentList).sorted()
            withContext(Dispatchers.Main) {
                differ.submitList(update)
            }
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

    fun getAdapterApplicationsCount(): Int = differ.currentList.size

    fun getAdapterApplicationId(position: Int): Long {
        return differ.currentList[position].hashCode().toLong()
    }

    override fun onShowOnlyApplicationsWithIndexChange(indexes: List<Int>) {
        launch {
            setAdapterApplications {
                indexes.map { index -> applications[index] }
            }
        }
    }

    override fun onStopFilteringApplications() {
        setAdapterApplications { this.applications }
    }
}