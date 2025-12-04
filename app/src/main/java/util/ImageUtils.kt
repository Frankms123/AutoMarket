package util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImageUtils {

    /**
     * Crea un archivo de imagen temporal en el directorio de caché de la aplicación.
     * Ideal para recibir la salida de la cámara.
     */
    fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = context.cacheDir
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    /**
     * Copia el contenido de una Uri (generalmente de la galería) a un nuevo archivo temporal.
     * Esto es necesario porque no podemos enviar directamente una Uri de contenido a Retrofit.
     * @return Un objeto File que apunta al archivo copiado, o null si falla.
     */
    fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val tempFile = createImageFile(context)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}