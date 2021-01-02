package juniojsv.minimum

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import juniojsv.minimum.ApplicationsEventHandler.Companion.DEFAULT_INTENT_FILTER
import juniojsv.minimum.databinding.ApplicationsFragmentBinding
import kotlinx.coroutines.*

class ApplicationsFragment : Fragment(), ApplicationsEventHandler.Listener, ApplicationsAdapter.OnHolderClick, SearchView.OnQueryTextListener, PopupMenu.OnMenuItemClickListener {
    private lateinit var binding: ApplicationsFragmentBinding
    private lateinit var preferences: SharedPreferences
    private lateinit var packageManager: PackageManager
    private val applicationsEventHandler = ApplicationsEventHandler(this)
    private val applicationsAdapter = ApplicationsAdapter(applications, this)
    private var queryChangedDebounceJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        packageManager = requireContext().packageManager
        context?.applicationContext?.registerReceiver(applicationsEventHandler, DEFAULT_INTENT_FILTER)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ApplicationsFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.mSearch.setOnQueryTextListener(this)

        binding.mApplications.apply {
            layoutManager = buildLayoutManager(this)
            adapter = applicationsAdapter
            setHasFixedSize(true)
        }

        binding.mApplicationsShowOptions.setOnClickListener { view ->
            PopupMenu(requireActivity(), view).also { popup ->
                popup.menu.apply {
                    addSubMenu(getString(R.string.expose)).apply {
                        add(0, MENU_ID_ALL_APPLICATIONS, 0, getString(R.string.all_applications))
                        add(0, MENU_ID_ONLY_BOOKMARKS, 1, getString(R.string.only_favorites))
                    }
                }
                popup.setOnMenuItemClickListener(this)
            }.show()
        }

        if (applications.isEmpty()) {
            getInstalledApplications(requireContext()) {
                applicationsAdapter.notifyDataSetChanged()
                binding.mApplicationsContainer.visibility = VISIBLE
                binding.mLoading.visibility = GONE
            }
        } else {
            binding.mApplicationsContainer.visibility = VISIBLE
            binding.mLoading.visibility = GONE
        }

    }

    private fun buildLayoutManager(listView: RecyclerView): RecyclerView.LayoutManager {
        return if (preferences.getBoolean("grid_view", false)) {
            listView.setPadding(0, 0, 0, 16.toDpi(requireContext()))
            GridLayoutManager(requireContext(), preferences.getInt("grid_view_columns", 3))
        } else {
            listView.addItemDecoration(
                    DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            LinearLayoutManager(requireContext())
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        context?.applicationContext?.unregisterReceiver(applicationsEventHandler)
    }

    override fun onApplicationAdded(intent: Intent) {
        val info = packageManager
                .getApplicationInfo(intent.dataString?.split(":")?.get(1) ?: "null", 0)
        info.toApplication(packageManager, 48.toDpi(requireContext()), true)?.let { application ->
            applications.add(application)
            applications.sort()
            if (!binding.mSearch.query.isNullOrEmpty())
                binding.mSearch.setQuery(null, false)

            applicationsAdapter.apply {
                setFilterQuery(String())
                filterViews()
            }
        }
    }

    override fun onApplicationRemoved(intent: Intent) {
        applications.removeByPackage(intent.dataString?.split(":")?.get(1) ?: "null")
        applications.sort()
        if (!binding.mSearch.query.isNullOrEmpty())
            binding.mSearch.setQuery(null, false)

        applicationsAdapter.apply {
            setFilterQuery(String())
            filterViews()
        }
    }

    override fun onApplicationIsChanged(intent: Intent) {
        GlobalScope.launch {
            val size = 48.toDpi(requireContext())
            intent.getStringArrayExtra(Intent.EXTRA_CHANGED_COMPONENT_NAME_LIST)?.forEach { packageName ->
                try {
                    val info = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                    if (info.enabled) {
                        val favorites = PreferenceManager
                                .getDefaultSharedPreferences(context).getStringSet("favorites", setOf())
                        applications.add(info.toApplication(packageManager, size, true)!!.apply {
                            isFavorite = favorites?.contains(packageName) ?: false
                        })
                    } else {
                        applications.removeByPackage(packageName)
                    }

                    applications.sort()

                    if (!binding.mSearch.query.isNullOrEmpty())
                        withContext(Dispatchers.Main) {
                            binding.mSearch.setQuery(null, false)
                        }

                    applicationsAdapter.apply {
                        setFilterQuery(String())
                        filterViews()
                    }

                } catch (e: PackageManager.NameNotFoundException) {
                    Log.e("onApplicationIsChanged", "Not found $packageName")
                }
            }
        }

    }

    override fun onClick(application: Application, view: View, position: Int) {
        application.apply {
            ActivityCompat.startActivity(requireActivity(), intent, ActivityOptionsCompat
                    .makeThumbnailScaleUpAnimation(view, application.icon, 0, 0).toBundle())
            if (isNew) {
                isNew = false
                applicationsAdapter.notifyItemChanged(position)
            }
        }
    }

    override fun onLongClick(application: Application, view: View, position: Int) {
        ApplicationActionsDialog(application, applicationsAdapter, position)
                .show(parentFragmentManager, "ApplicationActions")
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        applicationsAdapter.apply {
            setFilterQuery(query)
            filterViews()
        }

        return true
    }

    override fun onQueryTextChange(newText: String): Boolean {
        queryChangedDebounceJob?.cancel()

        queryChangedDebounceJob = GlobalScope.launch {
            delay(350L)
            onQueryTextSubmit(newText)
        }

        return true
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            MENU_ID_ALL_APPLICATIONS ->
                applicationsAdapter.apply {
                    if (!binding.mSearch.query.isNullOrEmpty())
                        binding.mSearch.setQuery(null, false)
                    setShowOnlyBookmarks(false)
                    filterViews()
                }
            MENU_ID_ONLY_BOOKMARKS ->
                applicationsAdapter.apply {
                    if (!binding.mSearch.query.isNullOrEmpty())
                        binding.mSearch.setQuery(null, false)
                    setShowOnlyBookmarks(true)
                    filterViews()
                }
        }

        return true
    }

    companion object {
        private val applications = ArrayList<Application>()
        private const val MENU_ID_ALL_APPLICATIONS = 1
        private const val MENU_ID_ONLY_BOOKMARKS = 2

        private fun getInstalledApplications(context: Context, onFinished: () -> Unit) =
                GlobalScope.launch {
                    val preferences = PreferenceManager
                            .getDefaultSharedPreferences(context)
                    val favorites = preferences.getStringSet("favorites", setOf())

                    context.packageManager?.apply {
                        getInstalledApplications(PackageManager.GET_META_DATA).forEach { info ->
                            info.toApplication(this, 48.toDpi(context))?.also { application ->
                                applications.add(application.apply {
                                    isFavorite = favorites?.contains(packageName) ?: false
                                })
                            }
                        }
                    }
                    applications.sort()
                    withContext(Dispatchers.Main) {
                        onFinished()
                    }
                }

    }
}