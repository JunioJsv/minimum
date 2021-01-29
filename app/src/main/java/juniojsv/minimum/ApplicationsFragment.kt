package juniojsv.minimum

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import juniojsv.minimum.ApplicationsEventHandler.Companion.DEFAULT_INTENT_FILTER
import juniojsv.minimum.databinding.ApplicationsFragmentBinding
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class ApplicationsFragment : Fragment(), ApplicationsEventHandler.Listener, ApplicationHolder.OnHolderClick, CoroutineScope {
    private lateinit var binding: ApplicationsFragmentBinding
    private lateinit var preferences: SharedPreferences

    private val applications = ArrayList<Application>()
    private val applicationsEventHandler = ApplicationsEventHandler(this)
    private val applicationsAdapter = ApplicationsAdapter(applications, this)

    private val job = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.IO + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        requireContext().registerReceiver(applicationsEventHandler, DEFAULT_INTENT_FILTER)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ApplicationsFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.mApplications.apply {
            layoutManager = preferenceLayoutManager
            adapter = applicationsAdapter
        }

        with(binding.mSearch) {
            maxWidth = Int.MAX_VALUE
            setOnQueryTextListener(applicationsAdapter.searchHandler)
            setOnSearchClickListener {
                binding.mApplicationsTitle.visibility = GONE
                binding.mApplicationsIcon.visibility = GONE
            }
            setOnCloseListener {
                binding.mApplicationsTitle.visibility = VISIBLE
                binding.mApplicationsIcon.visibility = VISIBLE
                false
            }
        }

        launch {
            applications.addAll(installedApplications)
            launch(Dispatchers.Main) {
                applicationsAdapter.notifyDataSetChanged()
                binding.mApplicationsContainer.visibility = VISIBLE
                binding.mLoading.visibility = GONE
            }
        }

    }

    private val preferenceLayoutManager: RecyclerView.LayoutManager
        get() {
            return if (preferences.getBoolean(PreferencesActivity.GRID_VIEW, false)) {
                with(binding.mApplications) {
                    if (itemDecorationCount > 0) {
                        removeItemDecorationAt(0)
                    }
                    setPadding(0, 0, 0,
                            resources.getDimensionPixelSize(R.dimen.dp16))
                }
                GridLayoutManager(requireContext(),
                        preferences.getInt(PreferencesActivity.GRID_VIEW_COLUMNS, 3))
            } else {
                binding.mApplications.addItemDecoration(
                        DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
                LinearLayoutManager(requireContext())
            }
        }

    private val installedApplications: ArrayList<Application>
        @SuppressLint("QueryPermissionsNeeded")
        get() {
            val applications = arrayListOf<Application>()
            val iconSize = resources.getDimensionPixelSize(R.dimen.dp48)

            with(requireContext().packageManager) {
                getInstalledApplications(PackageManager.GET_META_DATA).forEach { info ->
                    val intent = getLaunchIntentForPackage(info.packageName)
                    if (intent != null && info.packageName != BuildConfig.APPLICATION_ID) {
                        val application = Application(info, this, iconSize)
                        applications.add(application)
                    }
                }
                applications.sort()
            }

            return applications
        }

    override fun onApplicationAdded(intent: Intent) {
        launch {
            val iconSize = resources.getDimensionPixelSize(R.dimen.dp48)

            with(requireContext().packageManager) {
                val info = getApplicationInfo(
                        intent.data!!.encodedSchemeSpecificPart,
                        PackageManager.GET_META_DATA)

                applications.add(Application(info, this, iconSize, true))
                applications.sort()
            }

            for (index in 0..applications.size) {
                if (applications[index].packageName == intent.data?.encodedSchemeSpecificPart) {
                    launch(Dispatchers.Main) {
                        applicationsAdapter.notifyItemInserted(index)
                    }
                    break
                }
            }
        }
    }

    override fun onApplicationRemoved(intent: Intent) {
        launch {
            for (index in 0..applications.size) {
                if (applications[index].packageName == intent.data?.encodedSchemeSpecificPart) {
                    applications.removeAt(index)
                    launch(Dispatchers.Main) {
                        applicationsAdapter.notifyItemRemoved(index)
                    }
                    break
                }
            }
        }
    }

    override fun onApplicationIsChanged(intent: Intent) {}

    override fun onClick(application: Application, view: View, position: Int) {
        application.apply {
            ActivityCompat.startActivity(requireActivity(), intent, ActivityOptionsCompat
                    .makeThumbnailScaleUpAnimation(view, application.icon, 0, 0).toBundle())

            if (isNew) {
                isNew = false
                launch(Dispatchers.Main) {
                    applicationsAdapter.notifyItemChanged(position)
                }
            }
        }
    }

    override fun onLongClick(application: Application, view: View, position: Int) {
        ApplicationActionsDialog(application, position)
                .show(parentFragmentManager, "ApplicationActions")
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().unregisterReceiver(applicationsEventHandler)
    }
}