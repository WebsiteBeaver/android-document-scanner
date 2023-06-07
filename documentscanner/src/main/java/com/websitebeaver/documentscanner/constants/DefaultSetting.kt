package com.websitebeaver.documentscanner.constants

/**
 * This class contains default document scanner options
 */
class DefaultSetting {
    companion object {
        const val CROPPED_IMAGE_QUALITY = 100
        const val LET_USER_ADJUST_CROP = true
        const val MAX_NUM_DOCUMENTS = 24
        const val RESPONSE_TYPE = ResponseType.IMAGE_FILE_PATH
        const val IMAGE_PROVIDER = ImageProvider.CAMERA
    }
}