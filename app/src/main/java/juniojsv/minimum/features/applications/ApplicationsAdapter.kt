package juniojsv.minimum.features.applications

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import juniojsv.minimum.BuildConfig
import juniojsv.minimum.databinding.ApplicationGridVariantBinding
import juniojsv.minimum.databinding.ApplicationListVariantBinding
import juniojsv.minimum.models.Application
import juniojsv.minimum.models.ApplicationsGroup
import juniojsv.minimum.utils.UUIDSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume

@Serializable
private data class ApplicationState(
    val isPinned: Boolean,
    @Serializable(with = UUIDSerializer::class) val group: UUID?
)

@Serializable
private data class ApplicationsAdapterState(
    val groups: List<ApplicationsGroup>,
    val applications: Map<String, ApplicationState>
)

@SuppressLint("UnspecifiedRegisterReceiverFlag")
class ApplicationsAdapter(
    private val context: Context,
    private val lifecycle: Lifecycle,
    val callbacks: Callbacks
) : RecyclerView.Adapter<ApplicationBaseViewHolder>(),
    ApplicationsEventsBroadcastReceiver.Callbacks, DefaultLifecycleObserver, CoroutineScope {
    private val events = ApplicationsEventsBroadcastReceiver(lifecycle, this)
    val controller = ApplicationsAdapterController(this)
    private val lastPersistedState = async {
        suspendCancellableCoroutine {
            try {
                val file = context.openFileInput(STATE_FILE_NAME)
                val reader = file.reader()
                val json = reader.readText()
                reader.close()
                file.close()
                it.resume(Json.decodeFromString<ApplicationsAdapterState>(json))
            } catch (e: Throwable) {
                it.resume(null)
            }
        }
    }

    interface Callbacks : ApplicationBaseViewHolder.Callbacks

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + Job()

    init {
        setHasStableIds(true)
        lifecycle.addObserver(this)
        context.registerReceiver(
            events,
            ApplicationsEventsBroadcastReceiver.DEFAULT_INTENT_FILTER,
        )
    }

    private val isInitialized get() = controller.getInstalledApplicationsCount() > 0

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        lifecycle.removeObserver(this)
        context.unregisterReceiver(events)
        if (isInitialized) {
            val state = ApplicationsAdapterState(
                controller.getApplicationsGroups(),
                controller.getInstalledApplications().associate { application ->
                    application.packageName to ApplicationState(
                        application.isPinned,
                        application.group
                    )
                }
            )
            val file = context.openFileOutput(STATE_FILE_NAME, Context.MODE_PRIVATE)
            file.write(Json.encodeToString(state).toByteArray())
            file.close()
        }
    }

    private suspend fun getApplicationByInfo(info: ApplicationInfo) = coroutineScope {
        val packageManager = context.packageManager
        val packageName = info.packageName
        val launchIntent = packageManager.getLaunchIntentForPackage(info.packageName)
        val isMinimumAppPackage = packageName == BuildConfig.APPLICATION_ID
        var application: Application? = null
        if (!isMinimumAppPackage && launchIntent != null) {
            val label = info.loadLabel(packageManager) as String

            application = Application(label, packageName, launchIntent)
        }
        application
    }

    /**
     * Get all installed applications and persisted applications groups
     * setup applications list on [ApplicationsAdapterController]
     */
    suspend fun initialize() = coroutineScope {
        if (isInitialized) return@coroutineScope
        val lastState = lastPersistedState.await()
        val packageManager = context.packageManager
        val installedApplications = async {
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        }
        val deferreds = mutableListOf<Deferred<Application?>>()

        for (info in installedApplications.await()) {
            val state = lastState?.applications?.get(info.packageName)
            val isPinned = state?.isPinned == true
            deferreds.add(async {
                getApplicationByInfo(info)?.copy(isPinned = isPinned, group = state?.group)
            })
        }

        controller.setInstalledApplications(deferreds.awaitAll().filterNotNull())
        if (lastState?.groups != null) {
            controller.setApplicationsGroups(lastState.groups)
        }
    }

    private fun getInstalledApplicationIndexByPackageName(packageName: String): Int {
        return controller.getInstalledApplicationIndexByPackageName(packageName)
    }

    override fun getItemViewType(position: Int): Int {
        return when (controller.getAdapterItemAt(position)) {
            is Application -> ApplicationViewHolder.viewType
            is ApplicationsGroup -> ApplicationsGroupViewHolder.viewType
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ApplicationBaseViewHolder {
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

        return when (viewType) {
            ApplicationsGroupViewHolder.viewType -> ApplicationsGroupViewHolder(
                binding,
                callbacks,
                this::onApplicationsGroupChange
            )

            ApplicationViewHolder.viewType -> ApplicationViewHolder(
                binding,
                callbacks,
                this::onApplicationChange
            )

            else -> throw IllegalArgumentException()
        }
    }

    override fun getItemCount(): Int = controller.getAdapterItemsCount()

    override
    fun getItemId(position: Int): Long = controller.getAdapterItemId(position)

    override fun onBindViewHolder(
        holder: ApplicationBaseViewHolder,
        position: Int
    ) {
        holder.bind(controller.getAdapterItemAt(position))
    }

    fun onApplicationChange(application: Application) {
        val index = getInstalledApplicationIndexByPackageName(application.packageName)
        if (index != -1) {
            val previous = controller.getInstalledApplicationAt(index)
            launch {
                controller.setInstalledApplicationAt(index, application)
                // Notify change in group adapter item, to update group icon
                if (previous.group != application.group) {
                    previous.group?.let { id ->
                        withContext(Dispatchers.Main) {
                            notifyItemChanged(controller.getAdapterItemPositionById(id))
                        }
                    }
                }
            }
        }
    }

    private fun onApplicationsGroupChange(group: ApplicationsGroup) {
        val index = controller.getApplicationsGroupIndexById(group.id)
        if (index != -1) {
            launch { controller.setApplicationsGroupAt(index, group) }
        }
    }

    override suspend fun onApplicationAdded(intent: Intent): Unit = coroutineScope {
        val packageManager = context.packageManager
        intent.data?.schemeSpecificPart?.let { packageName ->
            val info = packageManager.getApplicationInfo(
                packageName,
                PackageManager.GET_META_DATA
            )
            getApplicationByInfo(info)?.let { application ->
                controller.addInstalledApplication(application.copy(isNew = true))
            }
        }
    }

    override fun isApplicationAlreadyAdded(packageName: String): Boolean {
        return getInstalledApplicationIndexByPackageName(packageName) != -1
    }

    override suspend fun onApplicationRemoved(intent: Intent): Unit = coroutineScope {
        intent.data?.schemeSpecificPart?.let { packageName ->
            val index = getInstalledApplicationIndexByPackageName(packageName)
            if (index != -1) {
                controller.removeInstalledApplicationAt(index)
            }
        }
    }

    override suspend fun onApplicationUpdated(intent: Intent): Unit = coroutineScope {
        intent.data?.schemeSpecificPart?.let { packageName ->
            val index = getInstalledApplicationIndexByPackageName(packageName)
            if (index != -1) {
                controller.setInstalledApplicationAt(
                    index,
                    controller.getInstalledApplicationAt(index).copy(isNew = true)
                )
            }
        }
    }

    override suspend fun onApplicationDisabled(intent: Intent) {
        onApplicationRemoved(intent)
    }

    companion object {
        private const val STATE_FILE_NAME = "applications_adapter_state.json"
    }
}
