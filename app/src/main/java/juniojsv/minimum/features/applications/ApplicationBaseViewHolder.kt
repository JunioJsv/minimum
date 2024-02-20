package juniojsv.minimum.features.applications

import android.graphics.Bitmap
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import juniojsv.minimum.models.Application
import juniojsv.minimum.models.ApplicationBase
import juniojsv.minimum.models.ApplicationsGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext


sealed class ApplicationBaseViewHolder(
    binding: ViewBinding,
) : RecyclerView.ViewHolder(binding.root), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + Job()

    interface Callbacks {
        fun getApplicationsGroupIcon(group: ApplicationsGroup): Bitmap
        fun getApplicationIcon(application: Application): Bitmap
        suspend fun onClickApplication(application: Application, view: View): Application?
        suspend fun onLongClickApplication(
            application: Application,
            view: View,
        ): Application?

        suspend fun onLongClickApplicationsGroup(
            group: ApplicationsGroup,
            view: View
        ): ApplicationsGroup?
    }

    abstract fun bind(item: ApplicationBase)
}