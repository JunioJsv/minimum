package juniojsv.minimum.features.applications

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
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
import kotlin.coroutines.CoroutineContext

@SuppressLint("UnspecifiedRegisterReceiverFlag")
class ApplicationsAdapter(
    private val context: Context,
    private val lifecycle: Lifecycle,
    private val callbacks: ApplicationViewHolder.Callbacks
) : RecyclerView.Adapter<ApplicationViewHolder>(),
    ApplicationsEventsBroadcastReceiver.Callbacks, DefaultLifecycleObserver, CoroutineScope {
    private var preferences: SharedPreferences
    private val events = ApplicationsEventsBroadcastReceiver(lifecycle, this)
    val controller = ApplicationsAdapterController(this)

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + Job()

    init {
        setHasStableIds(true)
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
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
        if (isInitialized)
            preferences.edit().apply {
                putStringSet(
                    context.getString(R.string.pref_applications_pinned_at_top_key),
                    controller.getPinnedApplicationsPackages().toSet()
                )
            }.apply()
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
     * Setup applications list on [ApplicationsAdapter]
     */
    suspend fun getInstalledApplications() = coroutineScope {
        val packageManager = context.packageManager
        val installedApplications = async {
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        }
        val pinnedApplications = preferences.getStringSet(
            context.getString(R.string.pref_applications_pinned_at_top_key),
            setOf()
        )
        val deferreds = mutableListOf<Deferred<Application?>>()

        for (info in installedApplications.await()) {
            val isPinned = pinnedApplications?.contains(info.packageName) == true
            deferreds.add(async { getApplicationByInfo(info)?.copy(isPinned = isPinned) })
        }

        controller.setInstalledApplications(deferreds.awaitAll().filterNotNull())
    }

    private fun getInstalledApplicationIndexByPackageName(packageName: String): Int {
        return controller.getInstalledApplicationIndexByPackageName(packageName)
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
        val index = getInstalledApplicationIndexByPackageName(application.packageName)
        if (index != -1) {
            launch { controller.setInstalledApplicationAt(index, application) }
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
}
