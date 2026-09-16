/*
 * Copyright 2025-2026 The FairScan authors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <https://www.gnu.org/licenses/>.
 */
package com.example.test.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.view.Choreographer
import android.view.View
import androidx.core.graphics.scale
import org.fairscan.imageprocessing.ImageSize
import org.fairscan.imageprocessing.Quad
import org.fairscan.imageprocessing.scaledTo

/**
 * View/XML thay thế cho @Composable AnalysisOverlay.
 * Gọi update(state, debugMode) mỗi khi CameraViewModel.liveAnalysisState phát giá trị mới.
 * (lerpQuad() được dùng lại từ QuadStabilizer.kt, cùng package nên không cần import.)
 */
class QuadOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    /** Đổi màu quad cho khớp theme của app (mặc định: primary màu tím Material). */
    var quadColor: Int = Color.parseColor("#6750A4")
        set(value) {
            field = value
            quadPaint.color = value
            invalidate()
        }

    private var maskSize: ImageSize? = null
    private var binaryMaskProvider: () -> Bitmap? = { null }
    private var debugMode: Boolean = false

    private var targetQuad: Quad? = null
    private var displayedQuad: Quad? = null

    private val quadPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = quadColor
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }
    private val maskPaint = Paint().apply {
        colorFilter = PorterDuffColorFilter(Color.argb(0x80, 0, 0xFF, 0), PorterDuff.Mode.SRC_IN)
    }

    private val frameCallback: Choreographer.FrameCallback = Choreographer.FrameCallback {
        val target = targetQuad
        if (target != null) {
            displayedQuad = displayedQuad?.let { lerpQuad(it, target, 0.15f) } ?: target
            invalidate()
            Choreographer.getInstance().postFrameCallback(frameCallback)
        }
    }

    /** Gọi từ Fragment/Activity: overlayView.update(liveAnalysisState, isDebugMode). */
    fun update(state: LiveAnalysisState, debugMode: Boolean) {
        this.maskSize = state.maskSize
        this.binaryMaskProvider = state.binaryMaskProvider
        this.debugMode = debugMode

        val newTarget = state.stableQuad
        if (newTarget == null) {
            val wasAnimating = targetQuad != null
            targetQuad = null
            displayedQuad = null
            if (wasAnimating) Choreographer.getInstance().removeFrameCallback(frameCallback)
            invalidate()
            return
        }

        val wasIdle = targetQuad == null
        targetQuad = newTarget
        if (wasIdle) {
            Choreographer.getInstance().postFrameCallback(frameCallback)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val maskSize = maskSize ?: return

        if (debugMode) {
            binaryMaskProvider()?.let { drawMask(canvas, it) }
        }

        displayedQuad?.let { quad ->
            val scaledQuad = quad.scaledTo(
                fromWidth = maskSize.width,
                fromHeight = maskSize.height,
                toWidth = width.toDouble(),
                toHeight = height.toDouble()
            )
            scaledQuad.edges().forEach {
                canvas.drawLine(
                    it.from.x.toFloat(), it.from.y.toFloat(),
                    it.to.x.toFloat(), it.to.y.toFloat(),
                    quadPaint
                )
            }
        }
    }

    private fun drawMask(canvas: Canvas, binaryMask: Bitmap) {
        if (width == 0 || height == 0) return
        val maskOverlay = replaceColor(binaryMask, Color.BLACK, Color.TRANSPARENT)
        val scaled = maskOverlay.scale(width, height)
        canvas.drawBitmap(scaled, 0f, 0f, maskPaint)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Choreographer.getInstance().removeFrameCallback(frameCallback)
    }
}

private fun replaceColor(bitmap: Bitmap, toReplace: Int, replacement: Int): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)

    val pixels = IntArray(width * height)
    result.getPixels(pixels, 0, width, 0, 0, width, height)

    for (i in pixels.indices) {
        if (pixels[i] == toReplace) {
            pixels[i] = replacement
        }
    }

    result.setPixels(pixels, 0, width, 0, 0, width, height)
    return result
}