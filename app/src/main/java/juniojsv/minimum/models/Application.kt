package juniojsv.minimum.models

import android.content.Intent
import android.graphics.Bitmap

data class Application(
    val label: String,
    val icon: Bitmap,
    val packageName: String,
    val intent: Intent,
    val isNew: Boolean = false,
    val isPinned: Boolean = false
) : Comparable<Application> {
    override fun compareTo(other: Application): Int {
        if (isPinned && !other.isPinned) {
            return -1
        } else if (!isPinned && other.isPinned) {
            return 1
        }

        return label.compareTo(other.label)
    }
}