package juniojsv.minimum.features.applications

import android.util.TypedValue
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.viewbinding.ViewBinding
import juniojsv.minimum.R
import juniojsv.minimum.models.ApplicationBase
import juniojsv.minimum.models.ApplicationsGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ApplicationsGroupViewHolder(
    private val binding: ViewBinding,
    private val callbacks: Callbacks,
    private val onChangeGroup: (group: ApplicationsGroup) -> Unit
) : ApplicationBaseViewHolder(binding) {
    override fun bind(item: ApplicationBase) {
        if (item !is ApplicationsGroup) throw IllegalArgumentException()
        with(binding.root) {
            findViewById<TextView>(R.id.label).text = item.label
            launch {
                val view = findViewById<ImageView>(R.id.icon)
                val animation = AlphaAnimation(0f, 1f).apply {
                    duration = 300
                }
                val icon = callbacks.getApplicationsGroupIcon(item)
                withContext(Dispatchers.Main) {
                    val padding = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        4f,
                        resources.displayMetrics
                    ).toInt()
                    view.background = resources.getDrawable(R.drawable.group_background)
                    view.setPadding(padding, padding, padding, padding)
                    view.startAnimation(animation)
                    view.setImageBitmap(icon)
                }
            }

            setOnClickListener {
                launch {
                    callbacks.onClickApplicationsGroup(item, it)?.let { group ->
                        withContext(Dispatchers.Main) {
                            onChangeGroup(group)
                        }
                    }
                }
            }

            setOnLongClickListener {
                launch {
                    callbacks.onLongClickApplicationsGroup(item, it)?.let { group ->
                        withContext(Dispatchers.Main) {
                            onChangeGroup(group)
                        }
                    }
                }
                true
            }
        }
    }

    companion object {
        const val viewType = 1
    }
}