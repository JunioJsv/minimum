package juniojsv.minimum

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment

class WidgetsActionsDialog(private val listener: DialogInterface.OnClickListener) : AppCompatDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            AlertDialog.Builder(it).apply {
                setItems(arrayOf(
                        getString(R.string.add_widget),
                        getString(R.string.change_wallpaper)
                ), listener)
            }.create()
        } ?: throw IllegalStateException("Activity can't be null")
    }
}