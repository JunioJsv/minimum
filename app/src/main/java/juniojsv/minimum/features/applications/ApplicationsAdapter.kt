package juniojsv.minimum.features.applications

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
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
    private val events = ApplicationsEventsBroadcastReceiver(this)
    private val applications = arrayListOf<Application>()
    val filter = ApplicationsAdapterFilter(applications, this)
    private var showOnlyApplicationsWithIndexes: List<Int>? = null

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + Job()

    init {
        setHasStableIds(true)
        context.registerReceiver(
            events,
            ApplicationsEventsBroadcastReceiver.DEFAULT_INTENT_FILTER,
        )
    }

    fun dispose() {
        context.unregisterReceiver(events)
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

    suspend fun fetchAllApplications() = coroutineScope {
        showOnlyApplicationsWithIndexes = null
        val packageManager = context.packageManager
        val installedApplications = async {
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        }
        val deferreds = mutableListOf<Deferred<Application?>>()

        for (info in installedApplications.await()) {
            deferreds.add(async { getApplication(info) })
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

    private fun getApplicationIndexByBindViewPositon(position: Int) =
        showOnlyApplicationsWithIndexes?.get(position) ?: position

    override fun onShowOnlyApplicationsWithIndexChange(indexes: List<Int>) {
        showOnlyApplicationsWithIndexes = indexes
        notifyDataSetChanged()
    }

    override fun onStopFilteringApplications() {
        showOnlyApplicationsWithIndexes = null
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = getApplicationsCount()

    override
    fun getItemId(position: Int): Long {
        val index = getApplicationIndexByBindViewPositon(position)

        return applications[index].hashCode().toLong()
    }

    private fun onApplicationChange(application: Application) {
        val index = getApplicationIndexByPackageName(application.packageName)
        if (index != -1) {
            val previous = applications[index]
            applications[index] = application
            if (previous.isPinned != application.isPinned) {
                applications.sort()
                notifyDataSetChanged()
            } else {
                notifyItemChanged(index)
            }
        }
    }

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        val index = getApplicationIndexByBindViewPositon(position)

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
}
