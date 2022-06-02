package com.websitebeaver.documentscanner.extensions

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.drawable.Drawable
import com.websitebeaver.documentscanner.models.Line
import com.websitebeaver.documentscanner.models.Quad

/**
 * This draws a quad (used to draw cropper). It draws 4 circles and 4 connecting lines
 *
 * @param quad 4 corners
 * @param pointRadius corner circle radius
 * @param paint quad style (color, thickness for example)
 */
fun Canvas.drawQuad(quad: Quad, pointRadius: Float, paint: Paint) {
  // draw 4 connecting lines
  for (edge: Line in quad.edges) {
    drawLine(edge.from.x, edge.from.y, edge.to.x, edge.to.y, paint)
  }

  // draw 4 corner points
  for ((_, cornerPoint: PointF) in quad.corners) {
    drawCircle(cornerPoint.x, cornerPoint.y, pointRadius, paint)
  }
}

/**
 * This draws the check icon on the finish document scan button. It's needed because the inner
 * circle covers the check icon.
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
