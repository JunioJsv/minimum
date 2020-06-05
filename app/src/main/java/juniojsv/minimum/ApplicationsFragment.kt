package juniojsv.minimum

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_DELETE
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import juniojsv.minimum.ApplicationsHandler.Companion.DEFAULT_INTENT_FILTER
import kotlinx.android.synthetic.main.applications_fragment.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ApplicationsFragment : Fragment(), ApplicationsHandler.Listener {
    private lateinit var preferences: SharedPreferences
    private val applicationsHandler = ApplicationsHandler(this)
    private val applicationsAdapter = ApplicationsAdapter(applications, object : ApplicationsAdapter.OnHolderClick {
        override fun onClick(application: Application, adapter: ApplicationsAdapter) {
            with(application) {
                activity?.startActivity(intent)
                if (isNew) {
                    isNew = false
                    adapter.notifyDataSetChanged()
                }
            }
        }

        override fun onLongClick(application: Application) {
            activity?.startActivity(
                    Intent(ACTION_DELETE, Uri.parse("package:${application.packageName}")))
        }
    })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.applications_fragment, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        with(mApplications) {
            if (preferences.getBoolean("grid_view", false)) {
                layoutManager = GridLayoutManager(requireContext(), 4).apply {
                    setPadding(0, 0, 0, (resources.displayMetrics.density * 16).toInt())
                }
            } else {
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(
                        DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            }
            adapter = applicationsAdapter
            setHasFixedSize(true)
            mSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    with(applicationsAdapter) {
                        filterViews(query) {
                            activity?.runOnUiThread {
                                notifyDataSetChanged()
                            }
                        }
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    with(applicationsAdapter) {
                        filterViews(newText) {
                            activity?.runOnUiThread {
                                notifyDataSetChanged()
                            }
                        }
                    }
                    return true
                }
            })
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

        activity?.registerReceiver(applicationsHandler, DEFAULT_INTENT_FILTER)
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.unregisterReceiver(applicationsHandler)
    }

    override fun onApplicationAdded(intent: Intent) {
        val iconSize = (resources.displayMetrics.density * 48).toInt()
        val packageManager = requireContext().packageManager
        val info = packageManager
                .getApplicationInfo(intent.dataString?.split(":")?.get(1) ?: "null", 0)
        info.toApplication(packageManager, iconSize, true)?.let { application ->
            applications.add(application)
            applications.sort()
            if (!mSearch.query.isNullOrEmpty())
                mSearch.setQuery(null, true)
            else
                applicationsAdapter.notifyDataSetChanged()
        }
    }

    override fun onApplicationRemoved(intent: Intent) {
        applications.removeByPackage(intent.dataString?.split(":")?.get(1) ?: "null")
        applications.sort()
        if (!mSearch.query.isNullOrEmpty())
            mSearch.setQuery(null, true)
        else
            applicationsAdapter.notifyDataSetChanged()
    }

    companion object {
        private val applications = arrayListOf<Application>()
        private fun getInstalledApplications(context: Context, onFinished: () -> Unit) {
            val iconSize = (context.resources.displayMetrics.density * 48).toInt()
            GlobalScope.launch {
                context.packageManager?.apply {
                    getInstalledApplications(PackageManager.GET_META_DATA).forEach { info ->
                        info.toApplication(this, iconSize)?.also { application ->
                            applications.add(application)
                        }
                    }
                }
                applications.sort()
                onFinished()
            }
        }
    }
}