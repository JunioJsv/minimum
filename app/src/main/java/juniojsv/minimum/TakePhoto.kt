package juniojsv.minimum

import android.app.Activity

import com.mindorks.paracamera.Camera

internal class TakePhoto(activity: Activity) {
    val camera: Camera = Camera.Builder()
            .resetToCorrectOrientation(true)
            .setTakePhotoRequestCode(1)
            .setName("IMG_" + System.currentTimeMillis())
            .setImageFormat(Camera.IMAGE_JPEG)
            .setCompression(70)
            .setImageHeight(1000)
            .build(activity)

    fun capture() {
        try {
            camera.takePicture()
        } catch (error: Exception) {
            error.printStackTrace()
        }
    }
}
