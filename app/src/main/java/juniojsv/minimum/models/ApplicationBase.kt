package juniojsv.minimum.models

sealed class ApplicationBase(open val label: String) : Comparable<ApplicationBase> {
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