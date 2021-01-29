package juniojsv.minimum

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

class ApplicationAdapterHolder(private val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
    interface OnHolderClick {
        fun onClick(application: Application, view: View, position: Int)
        fun onLongClick(application: Application, view: View, position: Int)
    }

    fun bind(application: Application, index: Int, onHolderClick: OnHolderClick) {
        with(binding.root) {
            findViewById<TextView>(R.id.mLabel).text = application.label
            findViewById<ImageView>(R.id.mIcon).setImageBitmap(application.icon)
            findViewById<ImageView>(R.id.mNew).visibility =
                    if (application.isNew) View.VISIBLE else View.GONE

            setOnClickListener {
                onHolderClick.onClick(application, it, index)
            }
            setOnLongClickListener {
                onHolderClick.onLongClick(application, it, index)
                true
            }
        }
    }
}