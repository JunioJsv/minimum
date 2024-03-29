package juniojsv.minimum.features.applications

import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.util.LruCache
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
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
import juniojsv.minimum.clear
import juniojsv.minimum.databinding.ApplicationsFragmentBinding
import juniojsv.minimum.models.Application
import juniojsv.minimum.models.ApplicationsGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ApplicationsFragment : Fragment(), ApplicationsAdapter.Callbacks, CoroutineScope {
    private lateinit var binding: ApplicationsFragmentBinding
    private lateinit var preferences: SharedPreferences
    private lateinit var applicationsAdapter: ApplicationsAdapter
    private lateinit var applicationsFilterAdapter: ApplicationsFilterAdapter
    private lateinit var packageManager: PackageManager
    private var imm: InputMethodManager? = null
    override val coroutineContext: CoroutineContext = Dispatchers.Default + Job()
    private val onBackPressCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (group != null) {
                onClearAllSelectionsOfAgroupMode()
                return
            }
            setApplicationsFilterViewClear()
        }
    }
    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            recyclerView.apply {
                val isKeyboardActive = imm?.isActive == true
                val isFilterViewVisible =
                    findViewById<SearchView>(R.id.applications_filter_button) != null
                val isScrollStateIdle = newState == RecyclerView.SCROLL_STATE_IDLE
                if (isScrollStateIdle && isKeyboardActive && !isFilterViewVisible) {
                    imm?.hideSoftInputFromWindow(windowToken, 0)
                }
            }
        }
    }

    // Utility var to keep group until will be added in adapter
    private var group: ApplicationsGroup? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        packageManager = requireContext().packageManager
        applicationsAdapter =
            ApplicationsAdapter(requireContext(), lifecycle, this).apply {
                launch {
                    initialize()
                    withContext(Dispatchers.Main) {
                        binding.applications.visibility = VISIBLE
                        binding.loading.visibility = GONE
                    }
                }
            }
        applicationsFilterAdapter =
            ApplicationsFilterAdapter(applicationsAdapter.controller.filter)
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
        binding.applications.apply {
            setItemViewCacheSize(20)
            addOnScrollListener(onScrollListener)
            layoutManager = getRecyclerViewLayoutManagerByPreferences()
            adapter = ConcatAdapter(ConcatAdapter.Config.Builder().apply {
                setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
            }.build(), applicationsFilterAdapter, applicationsAdapter)
        }
        requireActivity().onBackPressedDispatcher.addCallback(onBackPressCallback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onBackPressCallback.remove()
    }

    private fun getRecyclerViewLayoutManagerByPreferences(): RecyclerView.LayoutManager {
        with(binding.applications) {
            return if (preferences.getBoolean(
                    getString(R.string.pref_activate_grid_view_key),
                    false
                )
            ) {
                if (itemDecorationCount > 0) {
                    removeItemDecorationAt(0)
                }
                GridLayoutManager(
                    requireContext(),
                    preferences.getInt(getString(R.string.pref_grid_view_columns_count_key), 3)
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

    override fun getApplicationsGroupIcon(group: ApplicationsGroup): Bitmap {
        val controller = applicationsAdapter.controller
        val applications = controller.getInstalledApplications().filter { it.group == group.id }
        val icons = applications.sorted().take(4).map(this::getApplicationIcon)

        val size = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            48f,
            resources.displayMetrics
        ).toInt()

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)

        val iconSize = size / 2

        val iconsPerRow = size / iconSize

        var xPosition = 0
        var yPosition = 0

        for ((index, icon) in icons.withIndex()) {
            val scaledIcon = Bitmap.createScaledBitmap(icon, iconSize, iconSize, true)

            canvas.drawBitmap(scaledIcon, xPosition.toFloat(), yPosition.toFloat(), null)

            xPosition += iconSize

            if ((index + 1) % iconsPerRow == 0 || index == icons.lastIndex) {
                xPosition = 0
                yPosition += iconSize
            }
        }

        return bitmap
    }

    override fun getApplicationIcon(application: Application): Bitmap {
        val packageName = application.packageName
        return BitmapCache.get(packageName) ?: run {
            val size = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                48f,
                resources.displayMetrics
            ).toInt()
            val drawable = packageManager.getApplicationIcon(packageName)
            val bitmap = drawable.toBitmap(size, size)
            BitmapCache.put(packageName, bitmap)
            bitmap
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.applications_agroup_mode_shortcuts, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        with(applicationsAdapter.controller) {
            group?.also { group ->
                when (item.itemId) {
                    R.id.clear -> {
                        onClearAllSelectionsOfAgroupMode()
                    }

                    R.id.confirm -> {
                        onDisableAgroupMode()
                        if (
                            getApplicationsOnGroup(group.id).isNotEmpty() ||
                            getApplicationsGroupsToMergeWith(group.id).isNotEmpty()
                        )
                            launch {
                                addApplicationsGroup(group)
                            }
                    }
                }
            }
        }

        return true
    }

    private fun onClearAllSelectionsOfAgroupMode() {
        group?.also { target ->
            var cleanedCount = 0
            launch {
                with(applicationsAdapter.controller) {
                    forEachInstalledApplications {
                        var application = it
                        if (it.group == target.id) {
                            application = it.copy(group = null)
                            cleanedCount++
                        }
                        application
                    }
                    forEachApplicationsGroups {
                        var group = it
                        if (it.mergeWith == target.id) {
                            group = it.copy(mergeWith = null)
                            cleanedCount++
                        }
                        group
                    }
                    if (cleanedCount < 1) {
                        onDisableAgroupMode()
                    }
                }
            }
        }

    }

    private fun onEnableAgroupMode(group: ApplicationsGroup) {
        this.group = group
        setHasOptionsMenu(true)
    }

    private fun onDisableAgroupMode() {
        group = null
        setHasOptionsMenu(false)
    }

    override suspend fun onClickApplication(
        application: Application,
        view: View,
    ): Application? {
        group?.also { group ->
            return application.copy(
                group = if (application.group == group.id) null else group.id
            )
        }
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
        if (group != null) {
            continuation.resume(null)
            return@suspendCoroutine
        }
        var update: Application? = null
        ApplicationOptionsDialog(application, object : ApplicationOptionsDialog.Callbacks {
            override fun onEnableAgroupMode() {
                with(applicationsAdapter.controller) {
                    val label = getString(R.string.group) +
                            " ${getAdapterApplicationsGroupsCount() + 1}"
                    val group = ApplicationsGroup(label, isPinned = true)
                    onEnableAgroupMode(group)
                    update = application.copy(group = group.id)
                }
            }

            override fun onRemoveGroup() {
                update = application.copy(group = null)
            }

            override fun onTogglePinAtTop() {
                update = application.copy(isPinned = !application.isPinned)
            }

            override fun onDismiss() {
                continuation.resume(update)
            }
        }).show(parentFragmentManager, ApplicationOptionsDialog.TAG)
    }

    private suspend fun onUngroup(group: ApplicationsGroup) = coroutineScope {
        val controller = applicationsAdapter.controller
        val index = controller.getApplicationsGroupIndexById(group.id)
        if (index != -1) {
            controller.removeApplicationsGroupAt(index)
        }
    }

    override suspend fun onClickApplicationsGroup(
        group: ApplicationsGroup,
        view: View
    ): ApplicationsGroup? = coroutineScope {
        this@ApplicationsFragment.group?.let {
            return@coroutineScope group.copy(mergeWith = it.id)
        }
        suspendCoroutine<ApplicationsGroup?> { continuation ->

            var label: String? = null
            var update: ApplicationsGroup? = null
            ApplicationsGroupDialog(
                group,
                applicationsAdapter,
                object : ApplicationsGroupDialog.Callbacks {
                    override fun onEnableAddMode() {
                        val copy = group.copy(id = UUID.randomUUID())
                        onEnableAgroupMode(copy)
                        update = group.copy(mergeWith = copy.id)
                    }

                    override fun onUngroup() {
                        launch { onUngroup(group) }
                    }

                    override fun onChangeTitle(title: String) {
                        label = title
                    }

                    override fun onDismiss() {
                        label?.let {
                            if (it.isNotBlank() && it != group.label) {
                                update = group.copy(label = it)
                            }
                        }
                        continuation.resume(update)
                    }
                }).show(
                parentFragmentManager,
                ApplicationsGroupDialog.TAG,
            )
        }
    }

    override suspend fun onLongClickApplicationsGroup(
        group: ApplicationsGroup,
        view: View
    ): ApplicationsGroup? = coroutineScope {
        suspendCoroutine { continuation ->
            if (this@ApplicationsFragment.group != null) {
                continuation.resume(null)
                return@suspendCoroutine
            }

            var label: String? = null
            var update: ApplicationsGroup? = null
            ApplicationsGroupOptionsDialog(
                group,
                object : ApplicationsGroupOptionsDialog.Callbacks {
                    override fun onChangeTitle(title: String) {
                        label = title
                    }

                    override fun onEnableAddMode() {
                        val copy = group.copy(id = UUID.randomUUID())
                        onEnableAgroupMode(copy)
                        update = group.copy(mergeWith = copy.id)
                    }

                    override fun onUngroup() {
                        launch { onUngroup(group) }
                    }

                    override fun onTogglePinAtTop() {
                        update = group.copy(isPinned = !group.isPinned)
                    }

                    override fun onDismiss() {
                        label?.let {
                            if (it.isNotBlank() && it != group.label) {
                                update = group.copy(label = it)
                            }
                        }
                        continuation.resume(update)
                    }
                }).show(parentFragmentManager, ApplicationsGroupOptionsDialog.TAG)
        }
    }

    private fun setApplicationsFilterViewClear() {
        binding.applications.apply {
            findViewById<SearchView>(R.id.applications_filter_button)?.clear() ?: run {
                val onScrollListener = object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            recyclerView.findViewById<SearchView>(R.id.applications_filter_button)
                                ?.clear()
                            recyclerView.removeOnScrollListener(this)
                        }
                    }
                }
                addOnScrollListener(onScrollListener)
                smoothScrollToPosition(0)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.root.requestFocus()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.applications.removeOnScrollListener(onScrollListener)
        lifecycle.removeObserver(applicationsAdapter)
    }

    companion object {
        const val TAG = "ApplicationsFragment"
        private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()

        private object BitmapCache : LruCache<String, Bitmap>(maxMemory / 8) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
        }
    }
}