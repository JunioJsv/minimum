package juniojsv.minimum.features.applications

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.AsyncListDiffer.ListListener
import androidx.recyclerview.widget.GridLayoutManager
import juniojsv.minimum.R
import juniojsv.minimum.databinding.ApplicationsFragmentBinding
import juniojsv.minimum.databinding.ApplicationsGroupDialogTitleBinding
import juniojsv.minimum.models.Application
import juniojsv.minimum.models.ApplicationsGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class ApplicationsGroupDialog(
    private val group: ApplicationsGroup,
    private val applicationsAdapter: ApplicationsAdapter,
    private val callbacks: Callbacks,
) : AppCompatDialogFragment(), CoroutineScope {
    lateinit var binding: ApplicationsFragmentBinding
    lateinit var adapter: ApplicationsGroupAdapter
    private lateinit var onInstalledApplicationsListener: ApplicationsAdapterController.OnInstalledApplicationsListener
    private lateinit var adapterListListener: ListListener<Application>
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + Job()

    interface Callbacks {
        fun onUngroup()

        fun onChangeTitle(title: String)

        fun onDismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        callbacks.onDismiss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        adapter = ApplicationsGroupAdapter(
            applicationsAdapter.callbacks,
            applicationsAdapter::onApplicationChange
        )
        adapterListListener =
            ListListener<Application> { _, group ->
                if (group.isEmpty()) {
                    dismiss()
                    callbacks.onUngroup()
                }
            }
        adapter.addListListener(adapterListListener)
        onInstalledApplicationsListener =
            object : ApplicationsAdapterController.OnInstalledApplicationsListener {
                override fun onChange(applications: ArrayList<Application>) {
                    adapter.setApplications(
                        applicationsAdapter
                            .controller.getApplicationsOnGroup(group.id)
                    )
                }
            }
        applicationsAdapter.controller
            .addOnInstalledApplicationsListener(onInstalledApplicationsListener)
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        applicationsAdapter.controller
            .removeOnInstalledApplicationsListener(onInstalledApplicationsListener)
        adapter.removeListListener(adapterListListener)
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        launch {
            val applications = applicationsAdapter.controller.getApplicationsOnGroup(group.id)
            withContext(Dispatchers.Main) {
                adapter.setApplications(applications)
                binding.applications.visibility = View.VISIBLE
                binding.loading.visibility = View.GONE
            }
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext()).apply {
            setCustomTitle(ApplicationsGroupDialogTitleBinding.inflate(layoutInflater).run {
                title.setText(group.label)
                title.hint = group.label
                title.addTextChangedListener(onTextChanged = { text, _, _, _ ->
                    if (text != null)
                        callbacks.onChangeTitle(text.toString())
                })
                menu.setOnClickListener { view ->
                    PopupMenu(requireContext(), view).apply {
                        inflate(R.menu.applications_group_shortcuts)
                        gravity = Gravity.END
                        setOnMenuItemClickListener {
                            when (it.itemId) {
                                R.id.ungroup -> {
                                    this@ApplicationsGroupDialog.dismiss()
                                    callbacks.onUngroup()
                                    true
                                }

                                R.id.rename -> {
                                    val imm = ContextCompat.getSystemService(
                                        requireContext(),
                                        InputMethodManager::class.java
                                    )
                                    title.apply {
                                        requestFocus()
                                        setSelection(title.length())
                                        postDelayed({
                                            imm?.showSoftInput(
                                                title,
                                                InputMethodManager.SHOW_IMPLICIT
                                            )
                                        }, 100)
                                    }

                                    true
                                }

                                else -> false
                            }
                        }
                        show()
                    }
                }
                root
            })

            binding = ApplicationsFragmentBinding.inflate(layoutInflater)
            setView(binding.root.apply {
                val padding = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    24f,
                    resources.displayMetrics
                ).toInt()
                updatePadding(
                    top = padding,
                    bottom = padding
                )
            })
            binding.applications.layoutManager = GridLayoutManager(requireContext(), 3)
            binding.applications.adapter = adapter
        }.create()
    }

    companion object {
        const val TAG = "ApplicationsGroupDialog"
    }
}