import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileUtil {
    @Throws(IOException::class)
    fun createImageFile(activity: ComponentActivity, pageNumber: Int): File {
        val dateTime: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

        val contentResolver: ContentResolver = activity.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "DOCUMENT_SCAN_${pageNumber}_${dateTime}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw IOException("Failed to create image file")

        //Get the file path from Uri
        val filePath = getPathFromUri(activity, imageUri)
        return File(filePath)
    }

    private fun getPathFromUri(context: Context, uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            it.moveToFirst()
            return it.getString(it.getColumnIndexOrThrow("_data"))
        }
        throw IOException("Failed to get file path from Uri")
    }
}
