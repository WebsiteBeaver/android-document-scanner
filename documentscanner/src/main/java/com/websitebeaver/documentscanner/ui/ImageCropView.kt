package com.websitebeaver.documentscanner.ui

import android.graphics.*
import android.content.Context
import android.view.MotionEvent
import android.util.AttributeSet
import android.content.res.TypedArray
import android.annotation.SuppressLint
import com.websitebeaver.documentscanner.R
import androidx.appcompat.widget.AppCompatImageView
import com.websitebeaver.documentscanner.models.Quad
import com.websitebeaver.documentscanner.enums.QuadCorner
import com.websitebeaver.documentscanner.extensions.drawQuad

/**
 * This class contains the original document photo, and a cropper. The user can drag the corners
 * to make adjustments to the detected corners.
 *
 * @param context view context
 * @param attrs view attributes
 * @param defStyle view style
 * @constructor creates image crop view
 */
class ImageCropView @JvmOverloads constructor(context: Context, private val attrs: AttributeSet? = null, private val defStyle: Int = R.attr.ImageCropView) : AppCompatImageView(context, attrs, defStyle) {

    /**
     * @property shader for magnifier image.
     */
    private var shader: BitmapShader? = null

    /**
     * @property magnifierEnabled enable or disable magnifier circle.
     */
    var magnifierEnabled: Boolean = true

    /**
     * @property magnifierScale the scale of magnifier circle.
     */
    var magnifierScale: Float = 3f

    /**
     * @property magnifierRadius the size of magnifier circle.
     */
    var magnifierRadius: Float = 150f

    /**
     * @property edgeCornerRadius the size of corner circle.
     */
    var edgeCornerRadius: Float = 10f

    /**
     * @property quad the 4 document corners
     */
    private var quad: Quad? = null

    /**
     * @property prevTouchPoint keep track of where the user touches, so we know how much
     * to move corners on drag
     */
    private var prevTouchPoint: PointF? = null

    /**
     * @property closestCornerToTouch if the user touches close to the top left corner for
     * example, that corner should move on drag
     */
    private var closestCornerToTouch: QuadCorner? = null

    /**
     * @property cropper 4 corners and connecting lines
     */
    private val cropper = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * @property magnifierPaint selected corner for finding edges used for magnifier purpose.
     */
    private val magnifierPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * @property imagePreviewHeight this is needed because height doesn't update immediately
     * after we set the image
     */
    private var imagePreviewHeight = height

    /**
     * @property imagePreviewWidth this is needed because width doesn't update immediately
     * after we set the image
     */
    private var imagePreviewWidth = width

    /**
     * @property ratio image container height to image height ratio used to map container
     * to image coordinates and vice versa
     */
    private val ratio: Float get() = imagePreviewBounds.height() / drawable.intrinsicHeight

    /**
     * @property corners document corners in original image coordinates (original image is
     * probably bigger than the preview image)
     */
    val corners: Quad get() = quad!!.mapPreviewToOriginalImageCoordinates(
        imagePreviewBounds,
        ratio
    )

    init {
        val typedArray = getStyleTypedArray()

        // set cropper style
        applyPaintDefaultStyle(cropper, typedArray)
        applyPaintDefaultStyle(magnifierPaint, typedArray)
        magnifierScale = typedArray.getFloat(R.styleable.ImageCropView_magnifierScale, magnifierScale)
        edgeCornerRadius = typedArray.getFloat(R.styleable.ImageCropView_edgeCornerRadius, edgeCornerRadius)
        magnifierRadius = typedArray.getFloat(R.styleable.ImageCropView_magnifierRadius, magnifierRadius)
        magnifierEnabled = typedArray.getBoolean(R.styleable.ImageCropView_enableMagnifier, magnifierEnabled)
    }

    private fun getStyleTypedArray(): TypedArray {
        return context.obtainStyledAttributes(attrs, R.styleable.ImageCropView, defStyle, 0)
    }

    private fun applyPaintDefaultStyle(paint: Paint, typedArray: TypedArray) {
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.color = typedArray.getColor(R.styleable.ImageCropView_cornerStrokeColor, Color.WHITE)
        paint.strokeWidth = typedArray.getFloat(R.styleable.ImageCropView_cornerStrokeWidth, 6f)
    }

    /**
     * Initially the image preview height is 0. This calculates the height by using the photo
     * dimensions. It maintains the photo aspect ratio (we likely need to scale the photo down
     * to fit the preview container).
     *
     * @param photo the original photo with a rectangular document
     * @param screenWidth the device width
     */
    fun setImagePreviewBounds(photo: Bitmap, screenWidth: Int, screenHeight: Int) {
        // image width to height aspect ratio
        val imageRatio = photo.width.toFloat() / photo.height.toFloat()
        val buttonsViewMinHeight = context.resources.getDimension(
            R.dimen.buttons_container_min_height
        ).toInt()

        imagePreviewHeight = if (photo.height > photo.width) {
            // if user takes the photo in portrait
            (screenHeight.toFloat() / imageRatio).toInt()
        } else {
            // if user takes the photo in landscape
            (screenWidth.toFloat() * imageRatio).toInt()
        }

        // set a cap on imagePreviewHeight, so that the bottom buttons container isn't hidden
        imagePreviewHeight = Integer.min(
            imagePreviewHeight,
            screenHeight - buttonsViewMinHeight
        )

        imagePreviewWidth = screenWidth

        // image container initially has a 0 width and 0 height, calculate both and set them
        layoutParams.height = imagePreviewHeight
        layoutParams.width = imagePreviewWidth

        // For magnifier purpose
        if (magnifierEnabled) {
            val resizedBitmap = resizeBitmap(
                photo,
                imagePreviewWidth,
                imagePreviewHeight,
                screenWidth,
                screenHeight
            )
            shader = createBitmapShader(resizedBitmap!!)
        }

        // refresh layout after we change height
        requestLayout()
    }

    /**
     * Create a bitmap shader for magnifier corner.
     */
    private fun createBitmapShader(bitmap: Bitmap): BitmapShader {
        return BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    }

    /**
     * Resize bitmap to have an image in exact size of image view for calculate magnifier position.
     */
    private fun resizeBitmap(image: Bitmap, destWidth: Int, destHeight: Int, screenWidth: Int, screenHeight: Int): Bitmap? {
        val background = Bitmap.createBitmap(destWidth, destHeight, Bitmap.Config.ARGB_8888)
        val originalWidth = image.width.toFloat()
        val originalHeight = image.height.toFloat()
        val canvas = Canvas(background)
        val scaleX = screenWidth / originalWidth
        val scaleY = screenHeight / originalHeight
        var xTranslation = 0.0f
        var yTranslation = 0.0f
        var scale = 1f
        if (scaleX < scaleY) { // Scale on X, translate on Y
            scale = scaleX
            yTranslation = (destHeight - originalHeight * scale) / 2.0f
        } else { // Scale on Y, translate on X
            scale = scaleY
            xTranslation = (destWidth - originalWidth * scale) / 2.0f
        }
        val transformation = Matrix()
        transformation.postTranslate(xTranslation, yTranslation)
        transformation.preScale(scale, scale)

        val paint = Paint()
        paint.isFilterBitmap = true
        canvas.drawBitmap(image, transformation, paint)

        return background
    }

    /**
     * Once the user takes a photo, we try to detect corners. Those corners are in the original
     * photo coordinates. This function converts those coordinates to image preview container
     * coordinates, and stores them as quad
     *
     * @param cropperCorners 4 corner points in original photo coordinates
     */
    fun setCropper(cropperCorners: Quad) {
        // corner points are in original image coordinates, so we need to scale and move the
        // points to account for blank space (caused by photo and photo container having different
        // aspect ratios)
        quad = cropperCorners.mapOriginalToPreviewImageCoordinates(imagePreviewBounds, ratio)
    }

    /**
     * @property imagePreviewBounds image coordinates - if the image ratio is different than
     * the image container ratio then there's blank space either at the top and bottom of the
     * image or the left and right of the image
     */
    private val imagePreviewBounds: RectF
        get() {
            // image container width to height ratio
            val imageViewRatio: Float = imagePreviewWidth.toFloat() / imagePreviewHeight.toFloat()

            // image width to height ratio
            val imageRatio = drawable.intrinsicWidth.toFloat() / drawable.intrinsicHeight.toFloat()

            var left = 0f
            var top = 0f
            var right = imagePreviewWidth.toFloat()
            var bottom = imagePreviewHeight.toFloat()

            if (imageRatio > imageViewRatio) {
                // if the image is really wide, there's blank space at the top and bottom
                val offset = (imagePreviewHeight - (imagePreviewWidth / imageRatio)) / 2
                top += offset
                bottom -= offset
            } else {
                // if the image is really tall, there's blank space at the left and right
                // it's also possible that the image ratio matches the image container ratio
                // in which case there's no blank space
                val offset = (imagePreviewWidth - (imagePreviewHeight * imageRatio)) / 2
                left += offset
                right -= offset
            }

            return RectF(left, top, right, bottom)
        }

    /**
     * This ensures that the user doesn't drag a corner outside the image
     *
     * @param point a point
     * @return true if the point is inside the image preview container, false it's not
     */
    private fun isPointInsideImage(point: PointF): Boolean {
        if (point.x >= imagePreviewBounds.left
            && point.y >= imagePreviewBounds.top
            && point.x <= imagePreviewBounds.right
            && point.y <= imagePreviewBounds.bottom) {
            return true
        }

        return false
    }

    /**
     * This gets called constantly, and we use it to update the cropper corners
     *
     * @param canvas the image preview canvas
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (quad !== null) {
            // draw 4 corners and connecting lines
            canvas.drawQuad(quad!!, edgeCornerRadius, cropper, closestCornerToTouch, shader, magnifierPaint, magnifierRadius, magnifierScale)
        }

    }

    /**
     * This gets called when the user touches, drags, and stops touching screen. We use this
     * to figure out which corner we need to move, and how far we need to move it.
     *
     * @param event the touch event
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // keep track of the touched point
        val touchPoint = PointF(event.x, event.y)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // when the user touches the screen record the point, and find the closest
                // corner to the touch point
                prevTouchPoint = touchPoint
                closestCornerToTouch = quad!!.getCornerClosestToPoint(touchPoint)
            }
            MotionEvent.ACTION_UP -> {
                // when the user stops touching the screen reset these values
                prevTouchPoint = null
                closestCornerToTouch = null
                this.invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                // when the user drags their finger, update the closest corner position
                val touchMoveXDistance = touchPoint.x - prevTouchPoint!!.x
                val touchMoveYDistance = touchPoint.y - prevTouchPoint!!.y
                val cornerNewPosition = PointF(
                    quad!!.corners[closestCornerToTouch]!!.x + touchMoveXDistance,
                    quad!!.corners[closestCornerToTouch]!!.y + touchMoveYDistance
                )

                // make sure the user doesn't drag the corner outside the image preview container
                if (isPointInsideImage(cornerNewPosition)) {
                    quad!!.moveCorner(closestCornerToTouch!!, touchMoveXDistance, touchMoveYDistance)
                }

                // record the point touched, so we can use it to calculate how far to move corner
                // next time the user drags (assuming they don't stop touching the screen)
                prevTouchPoint = touchPoint

                // force refresh view
                invalidate()
            }
        }

        return true
    }
}