package com.websitebeaver.documentscanner

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.websitebeaver.documentscanner.constants.DefaultSetting
import com.websitebeaver.documentscanner.constants.DocumentScannerExtra
import com.websitebeaver.documentscanner.constants.ResponseType
import com.websitebeaver.documentscanner.extensions.toBase64
import com.websitebeaver.documentscanner.utils.ImageUtil
import java.io.File

/**
 * This class is used to start a document scan. It accepts parameters used to customize
 * the document scan, and callback parameters.
 *
 * @param activity the current activity
 * @param successHandler event handler that gets called on document scan success
 * @param errorHandler event handler that gets called on document scan error
 * @param cancelHandler event handler that gets called when a user cancels the document scan
 * @param responseType the cropped image gets returned in this format
 * @param letUserAdjustCrop whether or not the user can change the auto detected document corners
 * @param maxNumDocuments the maximum number of documents a user can scan at once
 * @constructor creates document scanner
 */
class DocumentScanner(
    private val activity: ComponentActivity,
    private val successHandler: ((documentScanResults: ArrayList<String>) -> Unit)? = null,
    private val errorHandler: ((errorMessage: String) -> Unit)? = null,
    private val cancelHandler: (() -> Unit)? = null,
    private var responseType: String? = null,
    private var letUserAdjustCrop: Boolean? = null,
    private var maxNumDocuments: Int? = null
) {
    init {
        responseType = responseType ?: DefaultSetting.RESPONSE_TYPE
    }

    /**
     * create intent to launch document scanner and set custom options
     */
    fun createDocumentScanIntent(): Intent {
        val documentScanIntent = Intent(activity, DocumentScannerActivity::class.java)
        documentScanIntent.putExtra(
            DocumentScannerExtra.EXTRA_LET_USER_ADJUST_CROP,
            letUserAdjustCrop
        )
        documentScanIntent.putExtra(
            DocumentScannerExtra.EXTRA_MAX_NUM_DOCUMENTS,
            maxNumDocuments
        )

        return documentScanIntent
    }

    /**
     * handle response from document scanner
     *
     * @param result the document scanner activity result
     */
     fun handleDocumentScanIntentResult(result: ActivityResult) {
        try {
            // make sure responseType is valid
            if (!arrayOf(
                    ResponseType.BASE64,
                    ResponseType.IMAGE_FILE_PATH
                ).contains(responseType)) {
                throw Exception("responseType must be either ${ResponseType.BASE64} " +
                        "or ${ResponseType.IMAGE_FILE_PATH}")
            }

            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    // check for errors
                    val error = result.data?.extras?.get("error") as String?
                    if (error != null) {
                        throw Exception("error - $error")
                    }

                    // get an array with scanned document file paths
                    val croppedImageResults: ArrayList<String> =
                        result.data?.getStringArrayListExtra(
                            "croppedImageResults"
                        ) ?: throw Exception("No cropped images returned")

                    // if responseType is imageFilePath return an array of file paths
                    var successResponse: ArrayList<String> = croppedImageResults

                    // if responseType is base64 return an array of base64 images
                    if (responseType == ResponseType.BASE64) {
                        val base64CroppedImages =
                            croppedImageResults.map { croppedImagePath ->
                                // read cropped image from file path, and convert to base 64
                                val base64Image = ImageUtil().readBitmapFromFileUriString(
                                    croppedImagePath,
                                    activity.contentResolver
                                ).toBase64()

                                // delete cropped image from android device to avoid
                                // accumulating photos
                                File(croppedImagePath).delete()

                                base64Image
                            }

                        successResponse = base64CroppedImages as ArrayList<String>
                    }

                    // trigger the success event handler with an array of cropped images
                    successHandler?.let { it(successResponse) }
                }
                Activity.RESULT_CANCELED -> {
                    // user closed camera
                    cancelHandler?.let { it() }
                }
            }
        } catch (exception: Exception) {
            // trigger the error event handler
            errorHandler?.let { it(exception.localizedMessage ?: "An error happened") }
        }
    }

    /**
     * add document scanner result handler and launch the document scanner
     */
    fun startScan() {
        activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult -> handleDocumentScanIntentResult(result) }
            .launch(createDocumentScanIntent())
    }
}