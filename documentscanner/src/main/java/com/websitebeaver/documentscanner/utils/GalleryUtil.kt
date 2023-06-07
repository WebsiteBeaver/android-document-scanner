package com.websitebeaver.documentscanner.utils

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.websitebeaver.documentscanner.extensions.saveToFile
import java.io.IOException
import java.io.File

/**
 * This class contains a helper function for opening the gallery.
 *
 * @param activity current activity
 * @param onGallerySuccess gets called with photo file path when photo is ready
 * @param onCancelGallery gets called when user cancels out of gallery
 * @constructor creates gallery util
 */
class GalleryUtil(
  private val activity: ComponentActivity,
  private val onGallerySuccess: (photoFilePath: String) -> Unit,
  private val onCancelGallery: () -> Unit
) {
  /** @property photoFilePath the photo file path */
  private lateinit var photoFilePath: String

  /** @property photoFile the photo file */
  private lateinit var photoFile: File

  /** @property startForResult used to launch gallery */
  private val startForResult =
    activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result: ActivityResult ->
      when (result.resultCode) {
        Activity.RESULT_OK -> {
          val data = result.data
          val uri = data?.data!!

          // create bitmap from image selection and save it to file
          ImageUtil().readBitmapFromFileUriString(
            uri.toString(),
            activity.contentResolver
          ).saveToFile(photoFile, 100)

          // send back photo file path on success
          onGallerySuccess(photoFilePath)
        }
        Activity.RESULT_CANCELED -> {
          // delete the photo since the user didn't finish choosing the photo
          File(photoFilePath).delete()
          onCancelGallery()
        }
      }
    }

  /**
   * open the gallery by launching an open document intent
   *
   * @param pageNumber the current document page number
   */
  @Throws(IOException::class)
  fun openGallery(pageNumber: Int) {
    // create intent to open gallery
    val openGalleryIntent = getGalleryIntent()

    // create new file for photo
    photoFile = FileUtil().createImageFile(activity, pageNumber)

    // store the photo file path, and send it back once the photo is saved
    photoFilePath = photoFile.absolutePath

    // open gallery
    startForResult.launch(openGalleryIntent)
  }

  private fun getGalleryIntent(): Intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
    type = "image/*"
    addCategory(Intent.CATEGORY_OPENABLE)
    addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
  }

}