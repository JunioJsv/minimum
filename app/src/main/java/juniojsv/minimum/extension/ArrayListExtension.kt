package juniojsv.minimum.extension

import android.util.Log
import juniojsv.minimum.Application

fun ArrayList<Application>.sort() {
    forEach { app ->
        if (!app.label[0].isUpperCase())
            app.label = app.label[0].toUpperCase() + app.label.substring(1)
    }
    sortWith(Comparator { app1, app2 ->
        app1.label.compareTo(app2.label)
    })
}

fun ArrayList<Application>.removeByPackage(packageName : String) {
    var target : Application? = null
    forEach { app ->
        if (app.packageName == packageName) target = app
    }
    target?.let { app -> remove(app) } ?: Log.e("removeByPackage", "Not found $packageName in ArrayList<App>")
}