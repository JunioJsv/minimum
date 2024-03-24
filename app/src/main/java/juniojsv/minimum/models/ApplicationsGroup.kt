package juniojsv.minimum.models

import com.google.gson.annotations.SerializedName
import java.util.UUID

/**
 * @param mergeWith is the id of target [ApplicationsGroup] to move all items of this group
 */
data class ApplicationsGroup(
    @SerializedName("group_label") override val label: String,
    @SerializedName("group_id") override val id: UUID = UUID.randomUUID(),
    val isPinned: Boolean,
    val mergeWith: UUID? = null
) : ApplicationBase(label, id) {
    override val priority: Int = if (isPinned) 1 else 0
}
