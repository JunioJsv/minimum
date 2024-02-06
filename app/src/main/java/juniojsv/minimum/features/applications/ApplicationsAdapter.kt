package juniojsv.minimum.features.applications

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import juniojsv.minimum.BuildConfig
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
import kotlin.coroutines.CoroutineContext

@SuppressLint("UnspecifiedRegisterReceiverFlag")
class ApplicationsAdapter(
    private val context: Context,
    private val callbacks: ApplicationViewHolder.Callbacks
) : RecyclerView.Adapter<ApplicationViewHolder>(),
    ApplicationsEventsBroadcastReceiver.Listener, CoroutineScope {
    private var preferences: SharedPreferences
    private val events = ApplicationsEventsBroadcastReceiver(this)
    val controller = ApplicationsAdapterController(this)

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
                controller.getPinnedApplicationsPackages().toSet()
            )
        }.apply()
    }

    private suspend fun getApplicationByInfo(info: ApplicationInfo) = coroutineScope {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(info.packageName)
        var application: Application? = null
        if (intent != null && info.packageName != BuildConfig.APPLICATION_ID) {
            val label = async { info.loadLabel(packageManager) as String }
            val packageName: String = info.packageName

            application = Application(label.await(), packageName, intent)
        }
        application
    }

    /**
     * Setup applications list on [ApplicationsAdapter]
     */
    suspend fun getInstalledApplications() = coroutineScope {
        val packageManager = context.packageManager
        val installedApplications = async {
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        }
        val pinnedApplications = preferences.getStringSet(PINNED_AT_TOP_APPLICATIONS, setOf())
        val deferreds = mutableListOf<Deferred<Application?>>()

        for (info in installedApplications.await()) {
            val isPinned = pinnedApplications?.contains(info.packageName) == true
            deferreds.add(async { getApplicationByInfo(info)?.copy(isPinned = isPinned) })
        }

        controller.setInstalledApplications(deferreds.awaitAll().filterNotNull())
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

        return ApplicationViewHolder(binding, callbacks)
    }

    override fun getItemCount(): Int = controller.getAdapterApplicationsCount()

    override
    fun getItemId(position: Int): Long = controller.getAdapterApplicationId(position)

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        holder.bind(
            controller.getAdapterApplicationAt(position),
            this::onApplicationChange
        )
    }

    private fun onApplicationChange(application: Application) {
        val index = controller.getInstalledApplicationIndexByPackageName(application.packageName)
        if (index != -1) {
            controller.setInstalledApplicationAt(index, application)
        }
    }

    override fun onApplicationAdded(intent: Intent) {
        launch {
            val packageManager = context.packageManager
            intent.data?.encodedSchemeSpecificPart?.let { packageName ->
                val info = packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.GET_META_DATA
                )
                getApplicationByInfo(info)?.let { application ->
                    controller.addInstalledApplication(application.copy(isNew = true))
                }
            }
        }
    }

    override fun onApplicationRemoved(intent: Intent) {
        launch {
            intent.data?.encodedSchemeSpecificPart?.let { packageName ->
                val index = controller.getInstalledApplicationIndexByPackageName(packageName)
                if (index != -1) {
                    controller.removeInstalledApplicationAt(index)
                }
            }
        }
    }

    override fun onApplicationUpdated(intent: Intent) {
        launch {
            intent.data?.encodedSchemeSpecificPart?.let { packageName ->
                val index = controller.getInstalledApplicationIndexByPackageName(packageName)
                if (index != -1) {
                    controller.setInstalledApplicationAt(
                        index,
                        controller.getInstalledApplicationAt(index).copy(isNew = true)
                    )
                }
            }
        }
    }

    private companion object Key {
        const val PINNED_AT_TOP_APPLICATIONS = "pinned_at_to_applications"
    }
}
