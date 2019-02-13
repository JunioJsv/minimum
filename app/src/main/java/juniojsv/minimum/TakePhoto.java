package juniojsv.minimum;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;

import com.mindorks.paracamera.Camera;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

    void moveBitmapToSdcard(File file) {
        File moveTo = new File(Environment.getExternalStorageDirectory().getPath() + "/Pictures/" + file.getName());

        try {
            InputStream inputStream =  new FileInputStream(file);
            OutputStream outputStream =  new FileOutputStream(moveTo);

            byte[] buffer = new byte[1024];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            file.delete();

        } catch (IOException error) {
            error.printStackTrace();
        }

    }

    Camera getCamera() {
        return this.camera;
    }
}
