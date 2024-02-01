package juniojsv.minimum.features.applications

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import juniojsv.minimum.R

class Application(context: Context, info: ApplicationInfo, var isNew: Boolean = false) :
    Comparable<Application> {

    private val packageManager = context.packageManager
    private val size = context.resources.getDimensionPixelSize(R.dimen.dp48)
    val label: String = info.loadLabel(packageManager) as String
    val icon: Bitmap = info.loadIcon(packageManager).toBitmap(size, size)
    val packageName: String = info.packageName
    val intent: Intent = packageManager.getLaunchIntentForPackage(packageName) ?: Intent()

    override fun compareTo(other: Application): Int = label.compareTo(other.label)

    override fun hashCode(): Int {
        var result = label.hashCode()
        result = 31 * result + icon.hashCode()
        result = 31 * result + intent.hashCode()
        result = 31 * result + packageName.hashCode()
        result = 31 * result + isNew.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Application

        if (label != other.label) return false
        if (icon != other.icon) return false
        if (intent != other.intent) return false
        if (packageName != other.packageName) return false
        if (isNew != other.isNew) return false

        return true
    }
}