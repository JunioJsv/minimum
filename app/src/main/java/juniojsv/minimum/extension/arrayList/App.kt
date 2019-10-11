package juniojsv.minimum.extension.arrayList

import juniojsv.minimum.App

fun ArrayList<App>.sort() {
    this.apply {
        forEach {
            if (!it.packageLabel[0].isUpperCase())
                it.packageLabel[0].toUpperCase()
        }
        sortWith(Comparator { app1, app2 ->
            app1.packageLabel.compareTo(app2.packageLabel)
        })
    }
}

fun ArrayList<App>.removeByPackage(packageName : String) {
    var target : App? = null
    this.forEach {
        if (it.packageName == packageName) target = it
    }
    remove(target!!)
}