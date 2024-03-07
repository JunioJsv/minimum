package juniojsv.minimum.models

import java.util.UUID

data class ApplicationsGroup(
    override val label: String,
    override val id: UUID = UUID.randomUUID(),
) : ApplicationBase(label, id) {
    override val priority: Int = 0
}
