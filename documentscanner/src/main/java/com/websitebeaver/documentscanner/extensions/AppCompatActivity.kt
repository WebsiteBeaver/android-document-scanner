package com.websitebeaver.documentscanner.extensions

import android.graphics.Rect
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity

@Suppress("DEPRECATION")
/** @property screenBounds the screen bounds (used to get screen width and height) */
val AppCompatActivity.screenBounds: Rect
  get() {
    // currentWindowMetrics was added in Android R
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
      return windowManager.currentWindowMetrics.bounds
    }

    // fall back to get screen width and height if using a version before Android R
    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    return Rect(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
  }

/** @property screenWidth the screen width */
val AppCompatActivity.screenWidth: Int
  get() = screenBounds.width()
