package juniojsv.minimum

interface MinimumInterface {

    var appsList: MutableList<App>

    fun notifyAdapter()

    fun onSearchAppsStarting()

    fun onSearchAppsFinished(newAppsList: MutableList<App>)
}
