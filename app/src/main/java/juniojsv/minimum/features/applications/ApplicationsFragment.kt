package juniojsv.minimum.features.applications

import android.animation.LayoutTransition
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import juniojsv.minimum.R
import juniojsv.minimum.databinding.ApplicationsFragmentBinding
import juniojsv.minimum.features.preferences.PreferencesActivity
import juniojsv.minimum.models.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ApplicationsFragment : Fragment(),
    ApplicationViewHolder.Callbacks, CoroutineScope {
    private lateinit var binding: ApplicationsFragmentBinding
    private lateinit var preferences: SharedPreferences
    private lateinit var applicationsAdapter: ApplicationsAdapter
    private lateinit var packageManager: PackageManager
    override val coroutineContext: CoroutineContext = Dispatchers.Default + Job()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        packageManager = requireContext().packageManager
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ApplicationsFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        applicationsAdapter =
            ApplicationsAdapter(requireContext(), this).apply {
                launch {
                    getInstalledApplications()
                    withContext(Dispatchers.Main) {
                        binding.applicationsContainer.visibility = VISIBLE
                        binding.loading.visibility = GONE
                    }
                }
            }

        binding.applications.apply {
            layoutManager = getRecyclerViewLayoutManagerByPreferences()
            adapter = applicationsAdapter
        }

        with(binding.searchIconButton) {
            layoutTransition = LayoutTransition()
            maxWidth = Int.MAX_VALUE
            setOnQueryTextListener(applicationsAdapter.controller.filter)
            setOnSearchClickListener {
                binding.searchTitle.visibility = GONE
            }
            setOnCloseListener {
                binding.searchTitle.visibility = VISIBLE
                false
            }
        }
    }

    private fun getRecyclerViewLayoutManagerByPreferences(): RecyclerView.LayoutManager {
        with(binding.applications) {
            return if (preferences.getBoolean(PreferencesActivity.GRID_VIEW, false)) {
                if (itemDecorationCount > 0) {
                    removeItemDecorationAt(0)
                }
                GridLayoutManager(
                    requireContext(),
                    preferences.getInt(PreferencesActivity.GRID_VIEW_COLUMNS, 3)
                )
            } else {
                addItemDecoration(
                    DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
                )
                LinearLayoutManager(requireContext())
            }
        }
    }

    override fun getApplicationIcon(application: Application): Bitmap {
        val packageName = application.packageName
        return BitmapCache.get(packageName) ?: run {
            val size = resources.getDimensionPixelSize(R.dimen.dp48)
            val drawable = packageManager.getApplicationIcon(packageName)
            val bitmap = drawable.toBitmap(size, size)
            BitmapCache.put(packageName, bitmap)
            bitmap
        }
    }

    override suspend fun onClickApplication(
        application: Application,
        view: View,
    ): Application? {
        try {
            startActivity(
                application.intent,
                ActivityOptionsCompat.makeTaskLaunchBehind().toBundle()
            )
        } catch (_: Throwable) {
        }


        if (application.isNew) {
            return application.copy(isNew = false)
        }
        return null
    }

    override suspend fun onLongClickApplication(
        application: Application,
        view: View,
    ): Application? = suspendCoroutine { continuation ->
        var update: Application? = null
        ApplicationOptionsDialog(application, object : ApplicationOptionsDialog.Callbacks {
            override fun onTogglePinAtTop() {
                update = application.copy(isPinned = !application.isPinned)
            }

            override fun onDismiss() {
                continuation.resume(update)
            }
        }).show(parentFragmentManager, ApplicationOptionsDialog.TAG)
    }


    override fun onStop() {
        super.onStop()
        with(binding.searchIconButton) {
            if (!isIconified) {
                onActionViewCollapsed()
                isIconified = true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        applicationsAdapter.dispose()
    }

    private companion object {
        private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()

        object BitmapCache : LruCache<String, Bitmap>(maxMemory / 8) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
        }
    }
}