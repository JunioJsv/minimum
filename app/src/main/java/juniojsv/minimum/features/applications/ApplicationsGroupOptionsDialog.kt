package juniojsv.minimum.features.applications

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.widget.addTextChangedListener
import juniojsv.minimum.R
import juniojsv.minimum.databinding.ApplicationsGroupOptionsDialogTitleBinding
import juniojsv.minimum.models.ApplicationsGroup
import juniojsv.minimum.models.LabeledCallback


class ApplicationsGroupOptionsDialog(
    private val group: ApplicationsGroup,
    private val callbacks: Callbacks
) : AppCompatDialogFragment() {

    interface Callbacks {
        fun onUngroup()

        fun onChangeTitle(title: String)

        fun onDismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        callbacks.onDismiss()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val options = arrayListOf(
            LabeledCallback(getString(R.string.ungroup), callbacks::onUngroup),
        )

        return AlertDialog.Builder(requireContext()).apply {
            setCustomTitle(ApplicationsGroupOptionsDialogTitleBinding.inflate(layoutInflater).run {
                title.setText(group.label)
                title.hint = group.label
                title.addTextChangedListener(onTextChanged = { text, _, _, _ ->
                    if (text != null)
                        callbacks.onChangeTitle(text.toString())
                })
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