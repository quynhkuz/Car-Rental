package com.example.test.crop

import android.graphics.PointF
import android.util.Size
import org.fairscan.imageprocessing.Point
import kotlin.math.sqrt

object QuadCoordinateUtils {

    fun calculateDisplaySize(bitmapWidth: Int, bitmapHeight: Int, containerSize: Size): Size {
        val imageAspectRatio = bitmapWidth.toFloat() / bitmapHeight.toFloat()
        val containerAspectRatio = containerSize.width / containerSize.height.toFloat()

        return if (imageAspectRatio > containerAspectRatio) {
            Size(containerSize.width, (containerSize.width / imageAspectRatio).toInt())
        } else {
            Size((containerSize.height * imageAspectRatio).toInt(), containerSize.height)
        }
    }

    fun normalizedToScreen(point: Point, containerSize: Size, displaySize: Size): PointF {
        val offsetX = (containerSize.width - displaySize.width) / 2
        val offsetY = (containerSize.height - displaySize.height) / 2
        return PointF(
            (point.x * displaySize.width).toFloat() + offsetX,
            (point.y * displaySize.height).toFloat() + offsetY
        )
    }

    fun screenDeltaToNormalized(dx: Float, dy: Float, displaySize: Size): PointF {
        return PointF(dx / displaySize.width, dy / displaySize.height)
    }

    fun getImageOffset(containerSize: Size, displaySize: Size): Size {
        return Size(
            (containerSize.width - displaySize.width) / 2,
            (containerSize.height - displaySize.height) / 2
        )
    }

    fun distance(a: PointF, b: PointF): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return sqrt(dx * dx + dy * dy)
    }
}