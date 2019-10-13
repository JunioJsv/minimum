package juniojsv.minimum.extension.arrayList

import android.util.Log
import juniojsv.minimum.App

fun ArrayList<App>.sort() {
    forEach { app ->
        if (!app.label[0].isUpperCase())
            app.label[0].toUpperCase()
    }
    sortWith(Comparator { app1, app2 ->
        app1.label.compareTo(app2.label)
    })
}

fun ArrayList<App>.removeByPackage(packageName : String) {
    var target : App? = null
    forEach { app ->
        if (app.packageName == packageName) target = app
    }
    target?.let { app -> remove(app) } ?: Log.e("removeByPackage", "Not found $packageName in ArrayList<App>")
}