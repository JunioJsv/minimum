package juniojsv.minimum.utilities

import android.os.AsyncTask
import android.util.Log

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class MoveFileTo(private val file: File, directory: File) : AsyncTask<Void, Void, Void>() {
    private val moveTo: File

    init {

        if (!directory.exists()) {
            if (directory.mkdir()) {
                Log.d(directory.name, "Dir created")
            } else
                Log.d(directory.name, "Dir cannot be created")
        }

        this.moveTo = File(directory.path + "/" + file.name)
    }

    override fun doInBackground(vararg voids: Void): Void? {

        try {
            val inputStream = FileInputStream(file)
            val outputStream = FileOutputStream(moveTo)

            val buffer = ByteArray(1024)
            val length: Int = inputStream.read(buffer)

            while (length > 0) {
                outputStream.write(buffer, 0, length)
            }

            inputStream.close()
            outputStream.close()

            file.delete()

        } catch (error: IOException) {
            error.printStackTrace()
        }

        return null
    }

    override fun onPostExecute(aVoid: Void) {
        super.onPostExecute(aVoid)
        if (moveTo.exists()) {
            Log.d(file.name, "Moved")
        } else
            Log.d(file.name, "Cannot be moved")
    }
}
