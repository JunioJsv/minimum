package juniojsv.minimum.models

data class LabeledCallback(
    val label: String,
    val enabled: Boolean = true,
    val callback: () -> Unit,
)
