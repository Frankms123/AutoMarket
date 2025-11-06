package util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImageUtils {

    /**
     * Guarda una imagen desde una Uri a un archivo permanente en el almacenamiento interno.
     * @param context Contexto de la aplicación.
     * @param uri La Uri de la imagen a guardar.
     * @return La ruta (path) del archivo guardado, o null si falla.
     */
    fun guardarImagenEnStorage(context: Context, uri: Uri): String? {
        return try {
            // Usar un nombre de archivo único basado en el tiempo
            val fileName = "IMG_${System.currentTimeMillis()}.jpg"
            val destinationFile = File(context.filesDir, fileName)

            // Copiar el contenido de la Uri al archivo de destino
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            destinationFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Elimina un archivo de imagen del almacenamiento interno.
     * @param imagePath La ruta del archivo a eliminar.
     */
    fun eliminarImagen(imagePath: String): Boolean {
        return try {
            val file = File(imagePath)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}