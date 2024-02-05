package juniojsv.minimum.features.applications

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import juniojsv.minimum.R
import juniojsv.minimum.models.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class ApplicationViewHolder(private val binding: ViewBinding) :
    RecyclerView.ViewHolder(binding.root), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + Job()

    interface Callbacks {
        suspend fun onClickApplication(application: Application, view: View): Application?
        suspend fun onLongClickApplication(
            application: Application,
            view: View,
        ): Application?
    }

    fun bind(
        application: Application,
        callbacks: Callbacks,
        onChangeApplication: (application: Application) -> Unit
    ) {
        with(binding.root) {
            findViewById<TextView>(R.id.label).text = application.label
            findViewById<ImageView>(R.id.icon).setImageBitmap(application.icon)
            findViewById<ImageView>(R.id.is_new).visibility =
                if (application.isNew) View.VISIBLE else View.GONE
            findViewById<ImageView>(R.id.is_pinned).visibility =
                if (application.isPinned) View.VISIBLE else View.GONE

            setOnClickListener {
                launch {
                    callbacks.onClickApplication(application, it)?.let { application ->
                        withContext(Dispatchers.Main) {
                            onChangeApplication(application)
                        }
                    }
                }
            }
            setOnLongClickListener {
                launch {
                    callbacks.onLongClickApplication(application, it)?.let { application ->
                        withContext(Dispatchers.Main) {
                            onChangeApplication(application)
                        }
                    }
                }
                true
            }
        }
    }
}