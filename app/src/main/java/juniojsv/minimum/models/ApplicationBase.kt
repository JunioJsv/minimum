package juniojsv.minimum.models

import java.util.UUID

sealed class ApplicationBase :
    Comparable<ApplicationBase> {
    abstract val label: String
    abstract val id: UUID

    /**
     * Sorting priority
     */
    abstract val priority: Int
    override fun compareTo(other: ApplicationBase): Int {
        return when {
            priority != other.priority -> other.priority.compareTo(priority)
            else -> label.compareTo(other.label)
        }
    }
}