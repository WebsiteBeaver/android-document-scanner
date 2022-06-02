package com.websitebeaver.documentscanner.models

/**
 * This class contains the original document photo, and a cropper. The user can drag the corners to
 * make adjustments to the detected corners.
 *
 * @param originalPhotoFilePath the photo file path before cropping
 * @param corners the document's 4 corner points
 * @constructor creates a document
 */
class Document(val originalPhotoFilePath: String, val corners: Quad) {}
