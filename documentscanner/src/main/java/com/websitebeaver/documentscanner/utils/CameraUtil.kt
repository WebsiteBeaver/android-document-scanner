package com.websitebeaver.documentscanner.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.io.IOException

/**
 * This class contains a helper function for opening the camera.
 *
 * @param activity current activity
 * @param onPhotoCaptureSuccess gets called with photo file path when photo is ready
 * @param onCancelPhoto gets called when user cancels out of camera
 * @constructor creates camera util
 */
class CameraUtil(
    private val activity: ComponentActivity,
    private val onPhotoCaptureSuccess: (photoFileUri: Uri) -> Unit,
    private val onCancelPhoto: () -> Unit
) {
    /**
     * @property photoFileUri the photo file Uri
     */
    private lateinit var photoFileUri: Uri

    /**
     * @property startForResult used to launch camera
     */
    private val startForResult = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            onPhotoCaptureSuccess(photoFileUri)
        } else {
            onCancelPhoto()
        }
    }

    /**
     * open the camera by launching an image capture intent
     *
     * @param pageNumber the current document page number
     */
    @Throws(IOException::class)
    fun openCamera(pageNumber: Int) {
        // create new file for photo
        val photoUri: Uri = FileUtil().createImageFile(activity, pageNumber)

        // store the photo file uri, and send it back once the photo is saved
        photoFileUri = photoUri

        // Create an intent to open the camera
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        }

        // open camera
        startForResult.launch(takePictureIntent)
    }
}
