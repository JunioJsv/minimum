package juniojsv.minimum

import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.util.Log

class TakePhoto(context: Context) {

    init {
        context.packageManager.apply {
            resolveActivity(Intent(MediaStore.ACTION_IMAGE_CAPTURE), 0)?.let { info ->
                (info.activityInfo?.applicationInfo ?:
                    info.serviceInfo?.applicationInfo ?:
                        info.providerInfo?.applicationInfo)?.let { camera ->
                            getLaunchIntentForPackage(camera.packageName)?.let { intent ->
                                context.startActivity(intent)
                            } ?: Log.d("TakePhoto", "Camera app has no launch intent")
                } ?: Log.d("TakePhoto", "Not found camera info")
            } ?: Log.d("TakePhoto", "No camera app installed")
        }

    }
}