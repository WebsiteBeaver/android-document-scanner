import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileUtil {
    @Throws(IOException::class)
    fun createImageFile(activity: ComponentActivity, pageNumber: Int): Bitmap {
        val dateTime: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

        val contentResolver: ContentResolver = activity.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "DOCUMENT_SCAN_${pageNumber}_${dateTime}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw IOException("Failed to create image file")

        return Bitmap.createBitmap(contentResolver.openInputStream(imageUri))
    }
}
