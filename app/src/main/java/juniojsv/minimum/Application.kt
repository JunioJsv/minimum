package juniojsv.minimum

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap

class Application(info: ApplicationInfo, packageManager: PackageManager, iconSize: Int, var isNew: Boolean = false) : Comparable<Application> {

    val label: String = info.loadLabel(packageManager) as String
    val icon: Bitmap = info.loadIcon(packageManager).toBitmap(iconSize, iconSize)
    val packageName: String = info.packageName
    val intent: Intent = packageManager.getLaunchIntentForPackage(packageName) ?: Intent()
    var isFavorite: Boolean = false

    override fun compareTo(other: Application): Int = label.compareTo(other.label)

    override fun hashCode(): Int {
        var result = label.hashCode()
        result = 31 * result + icon.hashCode()
        result = 31 * result + intent.hashCode()
        result = 31 * result + packageName.hashCode()
        result = 31 * result + isNew.hashCode()
        result = 31 * result + isFavorite.hashCode()
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
        if (isFavorite != other.isFavorite) return false

        return true
    }
}