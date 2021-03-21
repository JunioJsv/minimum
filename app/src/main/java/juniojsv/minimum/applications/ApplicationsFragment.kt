package juniojsv.minimum.applications

import android.animation.LayoutTransition
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import juniojsv.minimum.preferences.PreferencesActivity
import juniojsv.minimum.R
import juniojsv.minimum.applications.ApplicationsEventHandler.Companion.DEFAULT_INTENT_FILTER
import juniojsv.minimum.databinding.ApplicationsFragmentBinding
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class ApplicationsFragment : Fragment(), ApplicationsEventHandler.Listener, ApplicationAdapterHolder.HolderListener, CoroutineScope {
    private lateinit var binding: ApplicationsFragmentBinding
    private lateinit var preferences: SharedPreferences

    private lateinit var controller: Applications.Controller
    private lateinit var applicationsAdapter: ApplicationsAdapter

    private val eventHandler = ApplicationsEventHandler(this)
    private val job = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.IO + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        requireContext().registerReceiver(eventHandler, DEFAULT_INTENT_FILTER)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ApplicationsFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        launch {
            controller = Applications.getInstance(requireContext())
            applicationsAdapter = ApplicationsAdapter(controller.applications, this@ApplicationsFragment)

            withContext(Dispatchers.Main) {
                binding.mApplications.apply {
                    layoutManager = preferenceLayoutManager
                    adapter = applicationsAdapter
                }

                binding.mSearchBar.layoutTransition = LayoutTransition()

                with(binding.mSearch) {
                    maxWidth = Int.MAX_VALUE
                    setOnQueryTextListener(applicationsAdapter.searchHandler)
                    setOnSearchClickListener {
                        binding.mApplicationsTitle.visibility = GONE
                    }
                    setOnCloseListener {
                        binding.mApplicationsTitle.visibility = VISIBLE
                        false
                    }
                }

                binding.mApplicationsContainer.visibility = VISIBLE
                binding.mLoading.visibility = GONE
            }
        }
    }

    private val preferenceLayoutManager: RecyclerView.LayoutManager
        get() {
            with(binding.mApplications) {
                return if (preferences.getBoolean(PreferencesActivity.GRID_VIEW, false)) {
                    if (itemDecorationCount > 0) {
                        removeItemDecorationAt(0)
                    }
                    GridLayoutManager(requireContext(),
                            preferences.getInt(PreferencesActivity.GRID_VIEW_COLUMNS, 3))
                } else {
                    binding.mApplications.addItemDecoration(
                            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
                    LinearLayoutManager(requireContext())
                }
            }
        }

    override fun onApplicationAdded(intent: Intent) {
        launch {
            val iconSize = resources.getDimensionPixelSize(R.dimen.dp48)

            controller.apply {
                with(requireContext().packageManager) {
                    val info = getApplicationInfo(
                            intent.data!!.encodedSchemeSpecificPart,
                            PackageManager.GET_META_DATA)

                    addApplication(Application(requireContext(), info, true))
                }

                for (index in 0..applications.size) {
                    if (applications[index].packageName == intent.data?.encodedSchemeSpecificPart) {
                        with(applicationsAdapter.searchHandler) {
                            withContext(Dispatchers.Main) {
                                if (isSeeking)
                                    notifyDataSetChanged()
                                else
                                    applicationsAdapter.notifyItemInserted(index)
                            }
                        }
                        break
                    }
                }
            }
        }
    }

    override fun onApplicationRemoved(intent: Intent) {
        launch {
            controller.apply {
                for (index in 0..applications.size) {
                    if (applications[index].packageName == intent.data?.encodedSchemeSpecificPart) {
                        removeApplicationAt(index)
                        with(applicationsAdapter.searchHandler) {
                            withContext(Dispatchers.Main) {
                                if (isSeeking)
                                    notifyDataSetChanged()
                                else
                                    applicationsAdapter.notifyItemRemoved(index)
                            }
                        }
                        break
                    }
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

    override fun onLongClick(application: Application, view: View, position: Int) =
            ApplicationActionsDialog(application)
                    .show(parentFragmentManager, "ApplicationActions")


    override fun onStop() {
        super.onStop()
        with(binding.mSearch) {
            if (!isIconified) {
                onActionViewCollapsed()
                isIconified = true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().unregisterReceiver(eventHandler)
    }
}