package juniojsv.minimum.features.applications

import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
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
    private lateinit var applicationsFilterAdapter: ApplicationsFilterAdapter
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applicationsAdapter =
            ApplicationsAdapter(requireContext(), this).apply {
                launch {
                    getInstalledApplications()
                    withContext(Dispatchers.Main) {
                        binding.applications.visibility = VISIBLE
                        binding.loading.visibility = GONE
                    }
                }
            }
        applicationsFilterAdapter =
            ApplicationsFilterAdapter(applicationsAdapter.controller.filter)

        binding.applications.apply {
            layoutManager = getRecyclerViewLayoutManagerByPreferences()
            adapter = ConcatAdapter(ConcatAdapter.Config.Builder().apply {
                setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
            }.build(), applicationsFilterAdapter, applicationsAdapter)
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
                ).apply {
                    spanSizeLookup = object : SpanSizeLookup() {
                        init {
                            isSpanIndexCacheEnabled = true
                        }

                        override fun getSpanSize(position: Int): Int {
                            // On position 0 is filter bar view
                            return if (position == 0) spanCount else 1
                        }
                    }
                }
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
                application.launchIntent,
                null
            )
        } catch (throwable: Throwable) {
            Log.e(TAG, throwable.message, throwable)
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

    override fun onDestroy() {
        super.onDestroy()
        applicationsAdapter.dispose()
    }

    private companion object {
        private val TAG = this::class.java.name
        private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()

        object BitmapCache : LruCache<String, Bitmap>(maxMemory / 8) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
        }
    }
}