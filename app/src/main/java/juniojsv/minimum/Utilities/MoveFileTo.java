package juniojsv.minimum.Utilities;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MoveFileTo extends AsyncTask<Void, Void, Void> {
    private File file;
    private File moveTo;

    public MoveFileTo(File file, File directory) {
        this.file = file;

        if (!directory.exists()) {
            if (directory.mkdir()) {
                Log.d(directory.getName(),"Dir created");
            } else Log.d(directory.getName(), "Dir cannot be created");
        }

        this.moveTo = new File(directory.getPath() + "/" + file.getName());
    }

    @Override
    protected Void doInBackground(Void... voids) {

        try {
            InputStream inputStream = new FileInputStream(file);
            OutputStream outputStream = new FileOutputStream(moveTo);

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

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (moveTo.exists()) {
            Log.d(file.getName(), "Moved");
        } else Log.d(file.getName(), "Cannot be moved");
    }
}
