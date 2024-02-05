package juniojsv.minimum.features.applications

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import juniojsv.minimum.R
import juniojsv.minimum.models.Application

class ApplicationViewHolder(private val binding: ViewBinding) :
    RecyclerView.ViewHolder(binding.root) {
    interface Callbacks {
        fun onClickApplication(application: Application, view: View, position: Int): Application?
        fun onLongClickApplication(
            application: Application,
            view: View,
            position: Int
        ): Application?
    }

    fun bind(
        application: Application,
        index: Int,
        callbacks: Callbacks,
        onChangeApplication: (application: Application) -> Unit
    ) {
        with(binding.root) {
            findViewById<TextView>(R.id.label).text = application.label
            findViewById<ImageView>(R.id.icon).setImageBitmap(application.icon)
            findViewById<ImageView>(R.id.is_new).visibility =
                if (application.isNew) View.VISIBLE else View.GONE

            setOnClickListener {
                callbacks.onClickApplication(application, it, index)?.let(onChangeApplication)
            }
            setOnLongClickListener {
                callbacks.onLongClickApplication(application, it, index)?.let(onChangeApplication)
                true
            }
        }
    }
}