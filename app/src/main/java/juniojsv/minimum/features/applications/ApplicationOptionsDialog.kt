package juniojsv.minimum.features.applications

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.Intent.ACTION_DELETE
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import juniojsv.minimum.R
import juniojsv.minimum.models.Application
import juniojsv.minimum.models.LabeledCallback

class ApplicationOptionsDialog(
    private val application: Application,
    private val callbacks: Callbacks
) : AppCompatDialogFragment() {

    interface Callbacks {
        fun onTogglePinAtTop()
        fun onDismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        callbacks.onDismiss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val options = arrayListOf(
            LabeledCallback(
                if (application.isPinned) getString(R.string.unpin_of_top)
                else getString(R.string.pin_at_top),
                callbacks::onTogglePinAtTop
            ),
            LabeledCallback(getString(R.string.information)) {
                requireContext().startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:${application.packageName}")
                    )
                )
            },
            LabeledCallback(getString(R.string.uninstall)) {
                requireContext().startActivity(
                    Intent(
                        ACTION_DELETE,
                        Uri.parse("package:${application.packageName}")
                    )
                )
            },
        )

        return AlertDialog.Builder(requireContext()).apply {
            setTitle(application.label)
            setItems(
                options.map { it.label }.toTypedArray()
            ) { _, index -> options[index].callback() }
        }.create()
    }

    companion object {
        const val TAG = "ApplicationOptionsDialog"
    }
}