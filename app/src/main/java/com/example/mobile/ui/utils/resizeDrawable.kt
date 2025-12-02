package com.example.mobile.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

fun resizeDrawableResource(context: Context, resourceId: Int, widthDp: Int, heightDp: Int): Drawable? {
    val drawable = ContextCompat.getDrawable(context, resourceId) ?: return null

    val density = context.resources.displayMetrics.density
    val widthPx = (widthDp * density).toInt()
    val heightPx = (heightDp * density).toInt()

    if (drawable is BitmapDrawable) {
        val bitmap = drawable.bitmap
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, widthPx, heightPx, true)
        return BitmapDrawable(context.resources, scaledBitmap)
    }

    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return BitmapDrawable(context.resources, bitmap)
}