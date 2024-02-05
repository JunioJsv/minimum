package juniojsv.minimum.features.applications

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import juniojsv.minimum.BuildConfig
import juniojsv.minimum.R
import juniojsv.minimum.databinding.ApplicationGridVariantBinding
import juniojsv.minimum.databinding.ApplicationListVariantBinding
import juniojsv.minimum.models.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

@SuppressLint("UnspecifiedRegisterReceiverFlag")
class ApplicationsAdapter(
    private val context: Context,
    private val callbacks: ApplicationViewHolder.Callbacks
) : RecyclerView.Adapter<ApplicationViewHolder>(), ApplicationsAdapterFilter.Callbacks,
    ApplicationsEventsBroadcastReceiver.Listener, CoroutineScope {
    private var preferences: SharedPreferences
    private val events = ApplicationsEventsBroadcastReceiver(this)
    private val applications = arrayListOf<Application>()
    val filter = ApplicationsAdapterFilter(applications, this)
    private var showOnlyApplicationsWithIndexes: List<Int>? = null

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + Job()

    init {
        setHasStableIds(true)
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
        context.registerReceiver(
            events,
            ApplicationsEventsBroadcastReceiver.DEFAULT_INTENT_FILTER,
        )
    }

    fun dispose() {
        context.unregisterReceiver(events)
        preferences.edit().apply {
            putStringSet(
                PINNED_AT_TOP_APPLICATIONS,
                getPinnedApplicationsPackages().toSet()
            )
        }.apply()
    }

    private suspend fun getApplication(info: ApplicationInfo) = coroutineScope {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(info.packageName)
        var application: Application? = null
        if (intent != null && info.packageName != BuildConfig.APPLICATION_ID) {
            val label = async { info.loadLabel(packageManager) as String }
            val icon = async {
                val size = context.resources.getDimensionPixelSize(R.dimen.dp48)
                info.loadIcon(packageManager).toBitmap(size, size)
            }
            val packageName: String = info.packageName

            application = Application(label.await(), icon.await(), packageName, intent)
        }
        application
    }

    /**
     * Setup applications list on [ApplicationsAdapter]
     */
    suspend fun getInstalledApplications() = coroutineScope {
        showOnlyApplicationsWithIndexes = null
        val packageManager = context.packageManager
        val installedApplications = async {
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        }
        val pinnedApplications = preferences.getStringSet(PINNED_AT_TOP_APPLICATIONS, setOf())
        val deferreds = mutableListOf<Deferred<Application?>>()

        for (info in installedApplications.await()) {
            val isPinned = pinnedApplications?.contains(info.packageName) == true
            deferreds.add(async { getApplication(info)?.copy(isPinned = isPinned) })
        }

        applications.apply {
            clear()
            addAll(deferreds.awaitAll().filterNotNull())
            sort()
        }
        withContext(Dispatchers.Main) {
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationViewHolder {
        val recyclerView = parent as RecyclerView
        val layoutManager = recyclerView.layoutManager
        val layoutInflater = LayoutInflater.from(parent.context)

        val binding = when (layoutManager) {
            is GridLayoutManager -> ApplicationGridVariantBinding
                .inflate(
                    layoutInflater,
                    parent,
                    false
                )

            else -> ApplicationListVariantBinding
                .inflate(
                    layoutInflater,
                    parent,
                    false
                )
        }

        return ApplicationViewHolder(binding)
    }

    private val isFilteringApplications get() = showOnlyApplicationsWithIndexes?.isNotEmpty() == true
    private fun getApplicationsCount() = showOnlyApplicationsWithIndexes?.size ?: applications.size

    private fun getApplicationIndexByPackageName(packageName: String) =
        applications.indexOfFirst { it.packageName == packageName }

    private fun getApplicationIndexByBindViewPosition(position: Int) =
        showOnlyApplicationsWithIndexes?.get(position) ?: position

    override fun onShowOnlyApplicationsWithIndexChange(indexes: List<Int>) {
        showOnlyApplicationsWithIndexes = indexes
        notifyDataSetChanged()
    }

    private fun getPinnedApplicationsPackages() = applications.mapNotNull {
        var packageName: String? = null
        if (it.isPinned) {
            packageName = it.packageName
        }

        packageName
    }

    override fun onStopFilteringApplications() {
        showOnlyApplicationsWithIndexes = null
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = getApplicationsCount()

    override
    fun getItemId(position: Int): Long {
        val index = getApplicationIndexByBindViewPosition(position)

        return applications[index].hashCode().toLong()
    }

    private fun onApplicationChange(application: Application) {
        val index = getApplicationIndexByPackageName(application.packageName)
        if (index != -1) {
            val previous = applications[index]
            applications[index] = application
            if (previous.isPinned != application.isPinned) {
                launch {
                    applications.sort()
                    withContext(Dispatchers.Main) {
                        notifyDataSetChanged()
                    }
                }

            } else {
                notifyItemChanged(index)
            }
        }
    }

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        val index = getApplicationIndexByBindViewPosition(position)

        holder.bind(applications[index], callbacks, this::onApplicationChange)
    }

    override fun onApplicationAdded(intent: Intent) {
        launch {
            val packageManager = context.packageManager
            intent.data?.encodedSchemeSpecificPart?.let { packageName ->
                val info = packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.GET_META_DATA
                )
                getApplication(info)?.let { application ->
                    applications.add(application.copy(isNew = true))
                    applications.sort()
                    if (isFilteringApplications) {
                        filter.byLastQuery()
                    } else {
                        withContext(Dispatchers.Main) {
                            notifyItemInserted(
                                getApplicationIndexByPackageName(packageName)
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onApplicationRemoved(intent: Intent) {
        launch {
            intent.data?.encodedSchemeSpecificPart?.let { packageName ->
                val index = getApplicationIndexByPackageName(packageName)
                if (index != -1) {
                    applications.removeAt(index)
                    if (isFilteringApplications) {
                        filter.byLastQuery()
                    } else {
                        withContext(Dispatchers.Main) {
                            notifyItemRemoved(index)
                        }
                    }
                }
            }
        }
    }

    override fun onApplicationReplaced(intent: Intent) {
        launch {
            intent.data?.encodedSchemeSpecificPart?.let { packageName ->
                val index = getApplicationIndexByPackageName(packageName)
                if (index != -1) {
                    applications[index] = applications[index].copy(isNew = true)
                    withContext(Dispatchers.Main) {
                        notifyItemChanged(index)
                    }
                }
            }
        }
    }

    private companion object Key {
        const val PINNED_AT_TOP_APPLICATIONS = "pinned_at_to_applications"
    }
}
