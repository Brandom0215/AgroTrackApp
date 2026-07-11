package pa.ac.utp.agrotrackapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File

object ImageResizer {

    /**
     * Comprime y redimensiona una imagen desde una URI y la guarda en el archivo destino
     * en formato JPEG con un tamaño máximo especificado y 80% de calidad.
     */
    fun compressAndSaveImage(context: Context, sourceUri: Uri, destFile: File, maxSize: Int = 1024): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return false
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            // Calcular inSampleSize
            var inSampleSize = 1
            while ((options.outWidth / inSampleSize) > maxSize || (options.outHeight / inSampleSize) > maxSize) {
                inSampleSize *= 2
            }

            // Decodificar con el inSampleSize calculado
            val finalOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            val secondInputStream = context.contentResolver.openInputStream(sourceUri) ?: return false
            val bitmap = BitmapFactory.decodeStream(secondInputStream, null, finalOptions)
            secondInputStream.close()

            if (bitmap == null) return false

            // Redimensionar exactamente si es necesario
            val resizedBitmap = resizeBitmap(bitmap, maxSize)

            destFile.outputStream().use { out ->
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Comprime y redimensiona un Bitmap (usualmente de la cámara) y lo guarda en el archivo destino.
     */
    fun compressAndSaveBitmap(bitmap: Bitmap, destFile: File, maxSize: Int = 1024): Boolean {
        return try {
            val resizedBitmap = resizeBitmap(bitmap, maxSize)
            destFile.outputStream().use { out ->
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Carga un bitmap decodificado con opciones de escala a partir de una ruta de archivo
     * para evitar consumir excesiva memoria RAM.
     */
    fun decodeSampledBitmapFromFile(path: String, reqWidth: Int = 300, reqHeight: Int = 300): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(path, options)

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            BitmapFactory.decodeFile(path, options)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxSize && height <= maxSize) return bitmap

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        if (width > height) {
            newWidth = maxSize
            newHeight = (maxSize / ratio).toInt()
        } else {
            newHeight = maxSize
            newWidth = (maxSize * ratio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
