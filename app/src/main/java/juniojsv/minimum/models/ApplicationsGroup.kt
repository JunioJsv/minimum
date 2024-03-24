package juniojsv.minimum.models

import java.util.UUID

/**
 * @param mergeWith is the id of target [ApplicationsGroup] to move all items of this group
 */
data class ApplicationsGroup(
    override val label: String,
    override val id: UUID = UUID.randomUUID(),
    val isPinned: Boolean,
    val mergeWith: UUID? = null
) : ApplicationBase(label, id) {
    override val priority: Int = if (isPinned) 1 else 0
}
