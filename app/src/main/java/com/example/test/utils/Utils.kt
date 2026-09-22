package com.example.test.utils

import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.annotation.ColorInt






private val Float.dp: Float get() = this * Resources.getSystem().displayMetrics.density

fun View.setRoundedBackground(
    @ColorInt color: Int,
    radiusDp: Float = 0f,
    strokeWidthDp: Float = 0f,
    @ColorInt strokeColor: Int = 0
) {
    background = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(color)
        cornerRadius = radiusDp.dp
        if (strokeWidthDp > 0f) setStroke(strokeWidthDp.dp.toInt(), strokeColor)
    }
}