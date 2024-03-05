package juniojsv.minimum.features.applications

import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.viewbinding.ViewBinding
import juniojsv.minimum.R
import juniojsv.minimum.models.Application
import juniojsv.minimum.models.ApplicationBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ApplicationViewHolder(
    private val binding: ViewBinding,
    private val callbacks: Callbacks,
    private val onChangeApplication: (application: Application) -> Unit,
    private val isOnGroup: Boolean = false
) : ApplicationBaseViewHolder(binding) {
    override fun bind(item: ApplicationBase) {
        if (item !is Application) throw IllegalArgumentException()
        with(binding.root) {
            findViewById<TextView>(R.id.label).text = item.label
            launch {
                val view = findViewById<ImageView>(R.id.icon)
                val animation = AlphaAnimation(0f, 1f).apply {
                    duration = 300
                }
                val icon = callbacks.getApplicationIcon(item)
                withContext(Dispatchers.Main) {
                    view.startAnimation(animation)
                    view.setImageBitmap(icon)
                }
            }
            findViewById<ImageView>(R.id.is_new).visibility =
                if (item.isNew) View.VISIBLE else View.GONE
            findViewById<ImageView>(R.id.is_pinned).visibility =
                if (item.isPinned) View.VISIBLE else View.GONE
            findViewById<ImageView>(R.id.is_selected).visibility =
                if (item.group != null && !isOnGroup) View.VISIBLE else View.GONE

            setOnClickListener {
                launch {
                    callbacks.onClickApplication(item, it)?.let { application ->
                        withContext(Dispatchers.Main) {
                            onChangeApplication(application)
                        }
                    }
                }
            }
            setOnLongClickListener {
                launch {
                    callbacks.onLongClickApplication(item, it)?.let { application ->
                        withContext(Dispatchers.Main) {
                            onChangeApplication(application)
                        }
                    }
                }
                true
            }
        }
    }

    companion object {
        const val viewType = 0
    }
}