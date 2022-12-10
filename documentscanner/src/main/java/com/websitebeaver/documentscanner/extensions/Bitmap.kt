package com.websitebeaver.documentscanner.extensions

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * This converts the bitmap to base64
 *
 * @return image as a base64 string
 */
fun Bitmap.toBase64(quality: Int): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
    return Base64.encodeToString(
        byteArrayOutputStream.toByteArray(),
        Base64.DEFAULT
    )
}

/**
 * This converts the bitmap to base64
 *
 * @param file the bitmap gets saved to this file
 */
fun Bitmap.saveToFile(file: File, quality: Int) {
    val fileOutputStream = FileOutputStream(file)
    compress(Bitmap.CompressFormat.JPEG, quality, fileOutputStream)
    fileOutputStream.close()
}