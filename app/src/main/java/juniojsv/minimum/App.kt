package juniojsv.minimum

import android.content.Intent
import android.graphics.drawable.Drawable

data class App(var packageLabel: String, var icon: Drawable, var intent: Intent?, var packageName: String)