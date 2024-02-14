package juniojsv.minimum.features.applications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.LinkedList
import kotlin.coroutines.CoroutineContext

class ApplicationsEventsBroadcastReceiver(
    private val lifecycle: Lifecycle,
    private val callbacks: Callbacks
) : BroadcastReceiver(), DefaultLifecycleObserver,
    CoroutineScope {
    private val jobs = LinkedList<Job>()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + Job()

    private var isProcessingJobs = true


    interface Callbacks {
        suspend fun onApplicationAdded(intent: Intent)
        fun isApplicationAlreadyAdded(packageName: String): Boolean
        suspend fun onApplicationRemoved(intent: Intent)
        suspend fun onApplicationUpdated(intent: Intent)

        suspend fun onApplicationDisabled(intent: Intent)
    }

    init {
        lifecycle.addObserver(this)
        launch {
            while (isProcessingJobs) {
                Log.d(
                    this::class.java.name, "On $coroutineContext"
                )
                delay(100)
                jobs.poll()?.run {
                    start()
                    join()
                }
            }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        isProcessingJobs = false
        lifecycle.removeObserver(this)
        coroutineContext.cancel()
    }

    override fun onReceive(context: Context, intent: Intent) {
        val job = launch(start = CoroutineStart.LAZY) {
            val isReplacingPackage = intent
                .getBooleanExtra(Intent.EXTRA_REPLACING, false)
            when (intent.action) {
                Intent.ACTION_PACKAGE_ADDED -> {
                    if (isReplacingPackage) {
                        callbacks.onApplicationUpdated(intent)
                    } else {
                        callbacks.onApplicationAdded(intent)
                    }
                }

                Intent.ACTION_PACKAGE_REMOVED -> {
                    if (!isReplacingPackage)
                        callbacks.onApplicationRemoved(intent)
                }

                Intent.ACTION_PACKAGE_CHANGED -> {
                    val packageManager = context.packageManager
                    intent.data?.schemeSpecificPart?.let { packageName ->
                        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                        if (launchIntent == null) {
                            callbacks.onApplicationDisabled(intent)
                        } else if (!callbacks.isApplicationAlreadyAdded(packageName)) {
                            callbacks.onApplicationAdded(intent)
                        }
                    }
                }
            }
        }
        jobs.add(job)
    }

    companion object {
        val DEFAULT_INTENT_FILTER = IntentFilter().apply {
            addDataScheme("package")
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
        }
    }
}