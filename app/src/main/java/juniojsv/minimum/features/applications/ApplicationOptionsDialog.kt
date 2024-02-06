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
        return activity?.let { activity ->
            val options = arrayListOf(
                LabeledCallback(
                    if (application.isPinned) getString(R.string.action_unpin_of_top)
                    else getString(R.string.action_pin_at_top),
                    callbacks::onTogglePinAtTop
                ),
                LabeledCallback(getString(R.string.action_information)) {
                    activity.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:${application.packageName}")
                        )
                    )
                },
                LabeledCallback(getString(R.string.action_uninstall)) {
                    activity.startActivity(
                        Intent(
                            ACTION_DELETE,
                            Uri.parse("package:${application.packageName}")
                        )
                    )
                },
            )

            AlertDialog.Builder(activity).apply {
                setTitle(application.label)
                setItems(
                    options.map { it.label }.toTypedArray()
                ) { _, index -> options[index].callback() }
            }.create()
        } ?: throw IllegalStateException("Activity can't be null")
    }

    companion object {
        const val TAG = "ApplicationOptionsDialog"
    }
}