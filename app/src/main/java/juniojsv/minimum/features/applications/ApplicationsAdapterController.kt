package juniojsv.minimum.features.applications

import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import juniojsv.minimum.models.Application
import juniojsv.minimum.models.ApplicationBase
import juniojsv.minimum.models.ApplicationsGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.coroutines.CoroutineContext

class ApplicationsAdapterController(adapter: ApplicationsAdapter) :
    DiffUtil.ItemCallback<ApplicationBase>(), ApplicationsFilter.Callbacks, CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + Job()
    private val applications = arrayListOf<Application>()
    private val groups = arrayListOf<ApplicationsGroup>()
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

    suspend fun mapInstalledApplications(callback: (Application) -> Application) {
        val update = applications.map(callback)
        setInstalledApplications(update)
    }

    suspend fun addApplicationsGroup(group: ApplicationsGroup) {
        groups.add(group)
        onInstalledApplicationsChanged()
    }

    suspend fun removeApplicationsGroupAt(index: Int) {
        val removed = groups.removeAt(index)
        mapInstalledApplications {
            var application = it
            if (it.group == removed.uuid) {
                application = it.copy(group = null)
            }

            application
        }
    }

    suspend fun removeInstalledApplicationAt(index: Int) {
        applications.removeAt(index)
        onInstalledApplicationsChanged()
    }

    suspend fun setApplicationsGroupAt(index: Int, group: ApplicationsGroup) {
        groups[index] = group
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

    fun getApplicationsGroupIndexByUuid(uuid: UUID) = groups.indexOfFirst { it.uuid == uuid }

    private suspend fun setAdapterApplications(
        query: String? = null,
        callback: () -> List<Application>,
    ) = coroutineScope {
        val applications = callback().filter { application ->
            application.group == null || !groups.any { it.uuid == application.group }
        }
        val groups = if (query != null) groups.filter {
            it.label.contains(
                query,
                ignoreCase = true
            )
        } else groups
        val update = (applications + groups).sorted()
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
    fun getAdapterItemAt(position: Int): ApplicationBase = differ.currentList[position]
    override fun areItemsTheSame(oldItem: ApplicationBase, newItem: ApplicationBase): Boolean {
        return when {
            oldItem is Application && newItem is Application -> oldItem.packageName == newItem.packageName
            oldItem is ApplicationsGroup && newItem is ApplicationsGroup -> oldItem.uuid == newItem.uuid
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: ApplicationBase, newItem: ApplicationBase): Boolean {
        return oldItem == newItem
    }

    fun getInstalledApplicationsCount() = applications.size

    fun getInstalledApplications() = applications

    fun getAdapterItemsCount(): Int = differ.currentList.size

    fun getAdapterApplicationsGroupsCount(): Int = groups.size

    fun getAdapterItemId(position: Int): Long {
        return differ.currentList[position].hashCode().toLong()
    }

    override suspend fun onShowOnlyApplicationsWithIndex(indexes: List<Int>, query: String) {
        setAdapterApplications(query) {
            indexes.map { index -> applications[index] }
        }
    }

    override suspend fun onStopFilteringApplications() {
        setAdapterApplications { applications }
    }
}