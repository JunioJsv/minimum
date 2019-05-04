package juniojsv.minimum.utilities

import juniojsv.minimum.App
import java.util.*

class SortListOfApps(listToSort: MutableList<App>) {

    init {

        for (app in listToSort) {
            if (app.packageLabel[0] != Character.toUpperCase(app.packageLabel[0])) {
                app.packageLabel = app.packageLabel.substring(0, 1).toUpperCase() + app.packageLabel.substring(1)
            }
        }

        listToSort.sortWith(Comparator { app1, app2 ->
            val app1Label = app1.packageLabel
            val app2Label = app2.packageLabel
            app1Label.compareTo(app2Label)
        })

    }
}
