package juniojsv.minimum

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import juniojsv.minimum.ApplicationsEventHandler.Companion.DEFAULT_INTENT_FILTER
import kotlinx.android.synthetic.main.applications_fragment.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ApplicationsFragment : Fragment(), ApplicationsEventHandler.Listener, ApplicationsAdapter.OnHolderClick, SearchView.OnQueryTextListener, PopupMenu.OnMenuItemClickListener {
    private lateinit var preferences: SharedPreferences
    private val applicationsEventHandler = ApplicationsEventHandler(this)
    private val applicationsAdapter = ApplicationsAdapter(applications, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        activity?.registerReceiver(applicationsEventHandler, DEFAULT_INTENT_FILTER)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.applications_fragment, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mSearch.setOnQueryTextListener(this)

        mApplications.apply {
            layoutManager = buildLayoutManager(this)
            adapter = applicationsAdapter
            setHasFixedSize(true)
        }

        mApplicationsShowOptions.setOnClickListener { view ->
            PopupMenu(requireContext(), view).also { popup ->
                popup.menu.apply {
                    addSubMenu(getString(R.string.expose)).apply {
                        add(0, 1, 0, getString(R.string.all_applications))
                        add(0, 2, 1, getString(R.string.only_bookmarks))
                    }
                }
                popup.setOnMenuItemClickListener(this)
            }.show()
        }

        if (applications.isEmpty()) {
            getInstalledApplications(requireContext()) {
                activity?.runOnUiThread {
                    applicationsAdapter.notifyDataSetChanged()
                    mApplicationsContainer.visibility = VISIBLE
                    mLoading.visibility = GONE
                }
            }
        } else {
            mApplicationsContainer.visibility = VISIBLE
            mLoading.visibility = GONE
        }

    }

    private fun queryHandler(query: String?): Boolean =
            applicationsAdapter.run {
                filterViews(query) {
                    activity?.runOnUiThread {
                        notifyDataSetChanged()
                    }
                }
                true
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
        activity?.unregisterReceiver(applicationsEventHandler)
    }

    override fun onApplicationAdded(intent: Intent) {
        val packageManager = requireContext().packageManager
        val info = packageManager
                .getApplicationInfo(intent.dataString?.split(":")?.get(1) ?: "null", 0)
        info.toApplication(packageManager, 48.toDpi(requireContext()), true)?.let { application ->
            applications.add(application)
            applications.sort()
            if (!mSearch.query.isNullOrEmpty())
                mSearch.setQuery(null, false)

            applicationsAdapter.apply {
                filterViews {
                    activity?.runOnUiThread {
                        notifyDataSetChanged()
                    }
                }
            }
        }
    }

    override fun onApplicationRemoved(intent: Intent) {
        applications.removeByPackage(intent.dataString?.split(":")?.get(1) ?: "null")
        applications.sort()
        if (!mSearch.query.isNullOrEmpty())
            mSearch.setQuery(null, false)

        applicationsAdapter.apply {
            filterViews {
                activity?.runOnUiThread {
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun onClick(application: Application, adapter: ApplicationsAdapter, position: Int) {
        application.apply {
            activity?.startActivity(intent)
            if (isNew) {
                isNew = false
                adapter.notifyItemChanged(position)
            }
        }
    }

    override fun onLongClick(application: Application, position: Int) {
        ApplicationActionsDialog(application, applicationsAdapter, position)
                .show(parentFragmentManager, "ApplicationActions")
    }

    override fun onQueryTextSubmit(query: String?): Boolean = queryHandler(query)

    override fun onQueryTextChange(newText: String?): Boolean = queryHandler(newText)

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            1 ->
                applicationsAdapter.apply {
                    if (!mSearch.query.isNullOrEmpty())
                        mSearch.setQuery(null, false)
                    setShowOnlyBookmarks(false)
                    filterViews {
                        activity?.runOnUiThread {
                            notifyDataSetChanged()
                        }
                    }
                }
            2 ->
                applicationsAdapter.apply {
                    if (!mSearch.query.isNullOrEmpty())
                        mSearch.setQuery(null, false)
                    setShowOnlyBookmarks(true)
                    filterViews {
                        activity?.runOnUiThread {
                            notifyDataSetChanged()
                        }
                    }
                }
        }
        return true
    }

    companion object {
        private val applications = arrayListOf<Application>()
        private fun getInstalledApplications(context: Context, onFinished: () -> Unit) {
            val favorites = PreferenceManager
                    .getDefaultSharedPreferences(context).getStringSet("favorites", setOf())

            GlobalScope.launch {
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
                onFinished()
            }
        }
    }
}