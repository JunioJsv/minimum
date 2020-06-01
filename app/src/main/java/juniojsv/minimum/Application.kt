package juniojsv.minimum

import android.content.Intent
import android.graphics.Bitmap

class Application(
        var label: String,
        var icon: Bitmap,
        var intent: Intent,
        var packageName: String,
        var isNew: Boolean = false
) : Comparable<Application> {
    override fun compareTo(other: Application): Int = label.compareTo(other.label)
}