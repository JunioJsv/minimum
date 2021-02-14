package juniojsv.minimum.widgets

import android.app.Dialog
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import juniojsv.minimum.R

class WidgetsActionsDialog(private val listener: Listener) : AppCompatDialogFragment() {

    interface Listener {
        fun onActionSelected(index: Int)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            AlertDialog.Builder(it).apply {
                setItems(arrayOf(
                        getString(R.string.add_widget),
                        getString(R.string.change_wallpaper)
                ), DialogInterface.OnClickListener { _, index ->
                    listener.onActionSelected(index)
                })
            }.create()
        } ?: throw IllegalStateException("Activity can't be null")
    }
}