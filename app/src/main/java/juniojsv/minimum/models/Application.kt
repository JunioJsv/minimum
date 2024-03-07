package juniojsv.minimum.models

import android.content.Intent
import java.util.UUID

data class Application(
    override val label: String,
    val packageName: String,
    val launchIntent: Intent,
    val isNew: Boolean = false,
    val isPinned: Boolean = false,
    val group: UUID? = null,
    override val id: UUID = UUID.randomUUID()
) : ApplicationBase(label, id) {
    override val priority: Int = if (isPinned) 1 else 0
}