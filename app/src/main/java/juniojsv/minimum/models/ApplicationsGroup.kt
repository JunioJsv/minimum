package juniojsv.minimum.models

import java.util.UUID

data class ApplicationsGroup(
    override val label: String,
    val uuid: UUID = UUID.randomUUID()
) : ApplicationBase(label) {
    override val priority: Int = 0
}
