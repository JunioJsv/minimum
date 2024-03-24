package juniojsv.minimum.models

import juniojsv.minimum.utils.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * @param mergeWith is the id of target [ApplicationsGroup] to move all items of this group
 */
@Serializable
data class ApplicationsGroup(
    override val label: String,
    @Serializable(with = UUIDSerializer::class) override val id: UUID = UUID.randomUUID(),
    val isPinned: Boolean,
    @Serializable(with = UUIDSerializer::class) val mergeWith: UUID? = null
) : ApplicationBase() {
    override val priority: Int = if (isPinned) 1 else 0
}
