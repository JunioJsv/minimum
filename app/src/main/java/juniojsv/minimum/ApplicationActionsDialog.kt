package juniojsv.minimum

import android.app.Dialog
import android.content.Intent
import android.content.Intent.ACTION_DELETE
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment

class ApplicationActionsDialog(private val application: Application, private val position: Int) : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            AlertDialog.Builder(it).apply {
                setTitle(application.label)
                setItems(arrayOf(
                        getString(R.string.action_information),
                        getString(R.string.action_uninstall))) { _, index ->
                    when (index) {
                        0 -> {
                            activity?.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse("package:${application.packageName}")))
                        }
                        1 -> {
                            activity?.startActivity(Intent(ACTION_DELETE,
                                    Uri.parse("package:${application.packageName}")))
                        }
                    }
                }
            }.create()
        } ?: throw IllegalStateException("Activity can't be null")
    }
}