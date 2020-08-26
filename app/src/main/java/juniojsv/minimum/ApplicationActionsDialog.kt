package juniojsv.minimum

import android.app.Dialog
import android.content.Intent
import android.content.Intent.ACTION_DELETE
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.edit
import androidx.preference.PreferenceManager

class ApplicationActionsDialog(private val application: Application, private val adapter: ApplicationsAdapter, private val position: Int) : AppCompatDialogFragment() {
    private lateinit var preferences: SharedPreferences

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val favorites = preferences.getStringSet("favorites", setOf())
        val isFavorite = favorites?.contains(application.packageName) ?: false

        return activity?.let {
            AlertDialog.Builder(it).apply {
                setTitle(application.label)
                setItems(arrayOf(
                        if (!isFavorite) getString(R.string.action_add_bookmark) else getString(R.string.action_remove_bookmark),
                        getString(R.string.action_information),
                        getString(R.string.action_uninstall))) { _, index ->
                    when (index) {
                        0 -> {
                            if (!isFavorite) {
                                application.isFavorite = true
                                preferences.edit(commit = true) {
                                    putStringSet("favorites", favorites?.plus(application.packageName))
                                }
                            } else {
                                application.isFavorite = false
                                preferences.edit {
                                    putStringSet("favorites", favorites?.minus(application.packageName))
                                }
                            }
                            adapter.notifyItemChanged(position)
                        }
                        1 -> {
                            activity?.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse("package:${application.packageName}")))
                        }
                        2 -> {
                            activity?.startActivity(Intent(ACTION_DELETE,
                                    Uri.parse("package:${application.packageName}")))
                        }
                    }
                }
            }.create()
        } ?: throw IllegalStateException("Activity can't be null")
    }
}