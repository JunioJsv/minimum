package juniojsv.minimum;

import android.app.Activity;

import com.mindorks.paracamera.Camera;

public class TakePhoto {
    Camera camera;

    TakePhoto(Activity activity) {
        this.camera = new Camera.Builder()
                .resetToCorrectOrientation(true)
                .setTakePhotoRequestCode(1)
                .setName("IMG_" + System.currentTimeMillis())
                .setImageFormat(Camera.IMAGE_JPEG)
                .setCompression(70)
                .setImageHeight(1000)
                .build(activity);
    }

    void Capture() {
        try {
            camera.takePicture();
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    Camera getCamera() {
        return this.camera;
    }
}
