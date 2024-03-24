package juniojsv.minimum.features.applications

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.widget.addTextChangedListener
import juniojsv.minimum.R
import juniojsv.minimum.databinding.ApplicationsGroupDialogTitleBinding
import juniojsv.minimum.models.ApplicationsGroup
import juniojsv.minimum.models.LabeledCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class ApplicationsGroupOptionsDialog(
    private val group: ApplicationsGroup,
    private val callbacks: Callbacks,
) : AppCompatDialogFragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + Job()

    interface Callbacks {
        fun onChangeTitle(title: String)
        fun onEnableAgroupMode()
        fun onUngroup()
        fun onTogglePinAtTop()
        fun onDismiss()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        callbacks.onDismiss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val options = arrayListOf(
            LabeledCallback(
                getString(R.string.add),
                callback = callbacks::onEnableAgroupMode
            ),
            LabeledCallback(
                if (group.isPinned) getString(R.string.unpin_of_top)
                else getString(R.string.pin_at_top),
                callback = callbacks::onTogglePinAtTop
            ),
            LabeledCallback(
                getString(R.string.ungroup),
                callback = callbacks::onUngroup
            ),
        ).filter { it.enabled }

        return AlertDialog.Builder(requireContext()).apply {
            setCustomTitle(ApplicationsGroupDialogTitleBinding.inflate(layoutInflater).run {
                title.setText(group.label)
                title.hint = group.label
                title.addTextChangedListener(onTextChanged = { text, _, _, _ ->
                    if (text != null)
                        callbacks.onChangeTitle(text.toString())
                })
                menu.visibility = View.GONE
                root
            })
            setItems(
                options.map { it.label }.toTypedArray()
            ) { _, index -> options[index].callback() }
        }.create()
    }

    companion object {
        const val TAG = "ApplicationsGroupOptionsDialog"
    }
}