package juniojsv.minimum.utilities

import juniojsv.minimum.App
import java.util.*

class SortListOfApps(listToSort: MutableList<App>) {

    init {

        listToSort.forEach {
            if (it.packageLabel[0] != Character.toUpperCase(it.packageLabel[0])) {
                it.packageLabel = it.packageLabel.substring(0, 1).toUpperCase() + it.packageLabel.substring(1)
            }
        }

        listToSort.sortWith(Comparator { app1, app2 ->
            val app1Label = app1.packageLabel
            val app2Label = app2.packageLabel
            app1Label.compareTo(app2Label)
        })

    }
}
