package com.example.test.crop

import android.graphics.PointF
import android.util.Size
import org.fairscan.imageprocessing.Point
import org.fairscan.imageprocessing.Quad

class QuadEditingHandler {

    companion object {
        const val CORNER_RADIUS = 40f
        const val CORNER_TOUCH_RADIUS = 90f
    }

    fun findTouchedCorner(touchPos: PointF, quad: Quad, containerSize: Size, displaySize: Size): Int {
        return findTouchedCornerCandidates(touchPos, quad, containerSize, displaySize).firstOrNull() ?: -1
    }

    fun findTouchedCornerCandidates(
        touchPos: PointF, quad: Quad, containerSize: Size, displaySize: Size
    ): List<Int> {
        val corners = getCornerPositions(quad, containerSize, displaySize)
        return corners
            .mapIndexed { index, corner -> index to QuadCoordinateUtils.distance(touchPos, corner) }
            .filter { (_, distance) -> distance < CORNER_TOUCH_RADIUS }
            .sortedBy { (_, distance) -> distance }
            .map { (index, _) -> index }
    }

    /** [deltaX]/[deltaY] phải là delta đã normalize (screenDeltaToNormalized), không phải px thô. */
    fun updateQuadCorner(quad: Quad, cornerIndex: Int, deltaX: Float, deltaY: Float): Quad {
        val normalizedDelta = Point(deltaX.toDouble(), deltaY.toDouble())
        val candidate = when (cornerIndex) {
            0 -> quad.copy(topLeft = clampPoint(quad.topLeft + normalizedDelta))
            1 -> quad.copy(topRight = clampPoint(quad.topRight + normalizedDelta))
            2 -> quad.copy(bottomRight = clampPoint(quad.bottomRight + normalizedDelta))
            3 -> quad.copy(bottomLeft = clampPoint(quad.bottomLeft + normalizedDelta))
            else -> quad
        }
        return if (candidate.isConvex()) candidate else quad
    }

    private fun getCornerPositions(quad: Quad, containerSize: Size, displaySize: Size): List<PointF> {
        return listOf(
            QuadCoordinateUtils.normalizedToScreen(quad.topLeft, containerSize, displaySize),
            QuadCoordinateUtils.normalizedToScreen(quad.topRight, containerSize, displaySize),
            QuadCoordinateUtils.normalizedToScreen(quad.bottomRight, containerSize, displaySize),
            QuadCoordinateUtils.normalizedToScreen(quad.bottomLeft, containerSize, displaySize)
        )
    }

    private fun clampPoint(point: Point): Point {
        return Point(point.x.coerceIn(0.0, 1.0), point.y.coerceIn(0.0, 1.0))
    }

    private operator fun Point.plus(other: Point): Point {
        return Point(this.x + other.x, this.y + other.y)
    }
}