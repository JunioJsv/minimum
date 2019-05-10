package juniojsv.minimum

interface MinimumInterface {

    val appsList: MutableList<App>

    fun notifyAdapter()

    fun onSearchAppsStarting()

    fun onSearchAppsFinished(newAppsList: MutableList<App>)
}
