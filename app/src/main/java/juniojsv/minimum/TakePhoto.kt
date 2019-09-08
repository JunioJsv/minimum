package juniojsv.minimum

import android.Manifest.permission.CAMERA
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.os.Environment
import android.widget.Toast
import androidx.camera.core.CameraX
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureConfig
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File

class TakePhoto(minimumActivity: MinimumActivity) {

    init {
        if(ContextCompat.checkSelfPermission(minimumActivity, CAMERA) == PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(minimumActivity, WRITE_EXTERNAL_STORAGE) == PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(minimumActivity, arrayOf(CAMERA, WRITE_EXTERNAL_STORAGE), REQUEST_CODE)
        } else {
            CameraX.unbindAll()
            ImageCapture(ImageCaptureConfig.Builder()
                    .setTargetRotation(minimumActivity.windowManager.defaultDisplay.rotation)
                    .build()).apply {
                CameraX.bindToLifecycle(minimumActivity as LifecycleOwner, this)
                takePicture(createTempFile(
                        "${System.currentTimeMillis()}", ".jpg"
                ), object : ImageCapture.OnImageSavedListener {
                    override fun onImageSaved(file: File) {
                        file.apply {
                            copyTo(File(PATH + file.name))
                            delete()
                        }
                        Toast.makeText(minimumActivity, "Saved", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(imageCaptureError: ImageCapture.ImageCaptureError, message: String, cause: Throwable?) {
                        Toast.makeText(minimumActivity, "$message $cause", Toast.LENGTH_SHORT).show()
                    }

                })

            }
        }
    }

    companion object {
        val PATH = "${Environment.getExternalStorageDirectory().absolutePath}/Minimum/"
        val REQUEST_CODE = (0..0xff).random()
    }
}