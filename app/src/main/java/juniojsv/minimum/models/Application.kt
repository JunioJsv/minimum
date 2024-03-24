package juniojsv.minimum.models

import android.content.Intent
import com.google.gson.annotations.SerializedName
import java.util.UUID

data class Application(
    @SerializedName("application_label") override val label: String,
    val packageName: String,
    val launchIntent: Intent,
    val isNew: Boolean = false,
    val isPinned: Boolean = false,
    val group: UUID? = null,
    @SerializedName("application_id") override val id: UUID = UUID.randomUUID()
) : ApplicationBase(label, id) {
    override val priority: Int = if (isPinned) 1 else 0
}