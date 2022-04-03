package com.websitebeaver.documentscanner

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

/**
 * This class uses OpenCV to find document corners.
 *
 * @constructor creates document detector
 */
class DocumentDetector {

    /**
     * take a photo with a document, and find the document's corners
     *
     * @param image a photo with a document
     * @return a list with document corners (top left, top right, bottom right, bottom left)
     */
    fun findDocumentCorners(image: Bitmap): List<Point>? {

        // convert bitmap to OpenCV matrix
        val mat = Mat()
        Utils.bitmapToMat(image, mat)

        // shrink photo to make it easier to find document corners
        val shrunkImageHeight = 500.0
        Imgproc.resize(
            mat,
            mat,
            Size(
                shrunkImageHeight * image.width / image.height,
                shrunkImageHeight
            )
        )

        // convert photo to LUV colorspace to avoid glares caused by lights
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2Luv)

        // separate photo into 3 parts, (L, U, and V)
        val imageSplitByColorChannel: List<Mat> = mutableListOf()
        Core.split(mat, imageSplitByColorChannel)

        // find corners for each color channel, then pick the quad with the largest
        // area, and scale point to account for shrinking image before document detection
        val documentCorners: List<Point>? = imageSplitByColorChannel
            .mapNotNull { findCorners(it) }
            .maxByOrNull { Imgproc.contourArea(it) }
            ?.toList()
            ?.map {
                Point(
                    it.x * image.height / shrunkImageHeight,
                    it.y * image.height / shrunkImageHeight
                )
            }

        // sort points to force this order (top left, top right, bottom left, bottom right)
        return documentCorners
            ?.sortedBy { it.y }
            ?.chunked(2)
            ?.map { it.sortedBy { point -> point.x } }
            ?.flatten()
    }

    /**
     * take an image matrix with a document, and find the document's corners
     *
     * @param image a photo with a document in matrix format (only 1 color space)
     * @return a matrix with document corners or null if we can't find corners
     */
    private fun findCorners(image: Mat): MatOfPoint? {
        val outputImage = Mat()

        // blur image to help remove noise
        Imgproc.GaussianBlur(image, outputImage, Size(5.0, 5.0),0.0)

        // convert all pixels to either black or white (document should be black after this), but
        // there might be other parts of the photo that turn black
        Imgproc.threshold(
            outputImage,
            outputImage,
            0.0,
            255.0,
            Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU
        )

        // detect the document's border using the Canny edge detection algorithm
        Imgproc.Canny(outputImage, outputImage, 50.0, 200.0)

        // the detect edges might have gaps, so try to close those
        Imgproc.morphologyEx(
            outputImage,
            outputImage,
            Imgproc.MORPH_CLOSE,
            Mat.ones(Size(5.0, 5.0), CvType.CV_8U)
        )

        // get outline of document edges, and outlines of other shapes in photo
        val contours: MutableList<MatOfPoint> = mutableListOf()
        Imgproc.findContours(
            outputImage,
            contours,
            Mat(),
            Imgproc.RETR_LIST,
            Imgproc.CHAIN_APPROX_SIMPLE
        )

        // approximate outlines using polygons
        var approxContours = contours.map {
            val approxContour = MatOfPoint2f()
            val contour2f = MatOfPoint2f(*it.toArray())
            Imgproc.approxPolyDP(
                contour2f,
                approxContour,
                0.02 * Imgproc.arcLength(contour2f, true),
                true
            )
            MatOfPoint(*approxContour.toArray())
        }

        // We now have many polygons, so remove polygons that don't have 4 sides since we
        // know the document has 4 sides. Calculate areas for all remaining polygons, and
        // remove polygons with small areas. We assume that the document takes up a large portion
        // of the photo. Remove polygons that aren't convex since a document can't be convex.
        approxContours = approxContours.filter {
            it.height() == 4 && Imgproc.contourArea(it) > 1000 && Imgproc.isContourConvex(it)
        }

        // Once we have all large, convex, 4-sided polygons find and return the 1 with the
        // largest area
        return approxContours.maxByOrNull { Imgproc.contourArea(it) }
    }
}