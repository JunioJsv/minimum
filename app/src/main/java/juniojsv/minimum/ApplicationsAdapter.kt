package juniojsv.minimum

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.application.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ApplicationsAdapter(private val applications: ArrayList<Application>, private val onHolderClick: OnHolderClick) : RecyclerView.Adapter<ApplicationsAdapter.ApplicationHolder>() {
    private val showOnly = arrayListOf<Int>()
    private var filtering = false
    private var gridView = false

    interface OnHolderClick {
        fun onClick(application: Application, adapter: ApplicationsAdapter)
        fun onLongClick(application: Application)
    }

    class ApplicationHolder(private val view: View, private val adapter: ApplicationsAdapter) : RecyclerView.ViewHolder(view) {

        fun bind(application: Application, onHolderClick: OnHolderClick) {
            with(view) {
                mLabel.text = application.label
                mIcon.setImageBitmap(application.icon)
                mNew.visibility = if (application.isNew) View.VISIBLE else View.GONE

                setOnClickListener {
                    onHolderClick.onClick(application, adapter)
                }
                setOnLongClickListener {
                    onHolderClick.onLongClick(application)
                    true
                }
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        if (recyclerView.layoutManager is GridLayoutManager) gridView = true
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationHolder {
        return ApplicationHolder(
                LayoutInflater
                        .from(parent.context).inflate(
                                if (gridView) R.layout.application_grid_variant
                                else R.layout.application, parent, false), this)
    }

    override fun getItemCount(): Int =
            if (showOnly.isEmpty() && !filtering) applications.size else showOnly.size

    override fun onBindViewHolder(holder: ApplicationHolder, position: Int) {
        if (showOnly.isNotEmpty())
            holder.bind(applications[showOnly[position]], onHolderClick)
        else
            holder.bind(applications[position], onHolderClick)
    }

    fun filterViews(string: String?, onFinished: (showOnly: ArrayList<Int>) -> Unit) = GlobalScope.launch {
        showOnly.clear()
        if (!string.isNullOrEmpty()) {
            filtering = true
            applications.forEachIndexed { index, application ->
                if (application.label.contains(string, true))
                    showOnly.add(index)
            }
        } else
            filtering = false
        onFinished(showOnly)
    }

}
