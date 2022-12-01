package com.websitebeaver.documentscanner.extensions

import android.graphics.*
import android.graphics.drawable.Drawable
import com.websitebeaver.documentscanner.enums.QuadCorner
import com.websitebeaver.documentscanner.models.Line
import com.websitebeaver.documentscanner.models.Quad

/**
 * This draws a quad (used to draw cropper). It draws 4 circles and
 * 4 connecting lines
 *
 * @param quad 4 corners
 * @param pointRadius corner circle radius
 * @param paint quad style (color, thickness for example)
 */
fun Canvas.drawQuad(
    quad: Quad,
    pointRadius: Float,
    paint: Paint,
    selectedCorner: QuadCorner?,
    shader: BitmapShader?,
    magnifierPaint: Paint,
    magnifierRadius: Float,
    magnifierScale: Float
) {
    // draw 4 connecting lines
    for (edge: Line in quad.edges) {
        drawLine(edge.from.x, edge.from.y, edge.to.x, edge.to.y, paint)
    }

    // draw 4 corner points
    for ((corner, cornerPoint: PointF) in quad.corners) {
        if (corner == selectedCorner && shader != null) {
            drawMagnifier(cornerPoint, shader, magnifierRadius, magnifierPaint, magnifierScale)
        } else {
            drawCircle(cornerPoint.x, cornerPoint.y, pointRadius, paint)
        }
    }
}

fun Canvas.drawMagnifier(
    cornerPoint: PointF,
    shader: BitmapShader,
    pointRadius: Float,
    magnifierPaint: Paint,
    magnifierScale: Float
) {
    val myMatrix = Matrix()

    myMatrix.postScale(magnifierScale, magnifierScale, cornerPoint.x, cornerPoint.y)
    magnifierPaint.shader = shader
    magnifierPaint.shader.setLocalMatrix(myMatrix)
    magnifierPaint.style = Paint.Style.FILL
    this.drawCircle(cornerPoint.x, cornerPoint.y, pointRadius, magnifierPaint)
    this.save()

    magnifierPaint.shader = null
    magnifierPaint.style = Paint.Style.STROKE
    this.drawCircle(cornerPoint.x, cornerPoint.y, pointRadius, magnifierPaint)
}

/**
 * This draws the check icon on the finish document scan button. It's needed
 * because the inner circle covers the check icon.
 *
 * @param buttonCenterX the button center x coordinate
 * @param buttonCenterY the button center y coordinate
 * @param drawable the check icon
 */
fun Canvas.drawCheck(buttonCenterX: Float, buttonCenterY: Float, drawable: Drawable) {
    val mutate = drawable.constantState?.newDrawable()?.mutate()
    mutate?.setBounds(
        (buttonCenterX - drawable.intrinsicWidth.toFloat() / 2).toInt(),
        (buttonCenterY - drawable.intrinsicHeight.toFloat() / 2).toInt(),
        (buttonCenterX + drawable.intrinsicWidth.toFloat() / 2).toInt(),
        (buttonCenterY + drawable.intrinsicHeight.toFloat() / 2).toInt()
    )
    mutate?.draw(this)
}