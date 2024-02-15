package juniojsv.minimum.utils

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import juniojsv.minimum.R

class SetAsDefaultLauncherDialog(private val callbacks: Callbacks) : AppCompatDialogFragment() {

    abstract class Callbacks {
        open fun onPositiveButtonClicked() {}
        open fun onNegativeButtonClicked() {}
    }

    private fun openDefaultLauncherSettings() {
        val intent = Intent(Settings.ACTION_HOME_SETTINGS)
        startActivity(intent)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext()).apply {
            setTitle(getString(R.string.set_as_default_launcher))
            setMessage(getString(R.string.ask_to_set_as_default_launcher))
            setPositiveButton(R.string.yes) { dialog, _ ->
                callbacks.onPositiveButtonClicked()
                openDefaultLauncherSettings()
                dialog.dismiss()
            }
            setNegativeButton(R.string.no) { dialog, _ ->
                callbacks.onNegativeButtonClicked()
                dialog.dismiss()
            }
        }.create()
    }

    companion object {
        const val TAG = "SetAsDefaultLauncherDialog"
    }
}