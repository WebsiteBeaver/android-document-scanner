package com.websitebeaver.documentscanner.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.IOException


class FileUtil {
    @Throws(IOException::class)
    fun createImageFile(activity: ComponentActivity, pageNumber: Int): Uri {
        val dateTime: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val contentResolver: ContentResolver = activity.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "DOCUMENT_SCAN_${pageNumber}_${dateTime}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/MyAPP")
        }

        val imageUri: Uri? = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (imageUri != null) {
            return imageUri
        } else {
            throw IOException("Failed to create image file")
        }
    }
}