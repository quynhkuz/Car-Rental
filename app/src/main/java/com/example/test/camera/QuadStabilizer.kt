package com.example.test.camera

import org.fairscan.imageprocessing.Point
import org.fairscan.imageprocessing.Quad
import org.fairscan.imageprocessing.norm

class QuadStabilizer {

    private var stableCount = 0
    private var lastRawQuad: Quad? = null

    fun update(rawQuad: Quad?): Quad? {
        val previousQuad = lastRawQuad
        lastRawQuad = rawQuad

        if (rawQuad == null) {
            stableCount = 0
            return null
        }

        if (previousQuad == null) {
            stableCount = 1
            return null
        }

        val dist = previousQuad.maxCornerDistanceTo(rawQuad)
        // 20f is based on the assumption that the preview has a size of 640×480
        if (dist < 20f) {
            stableCount++
        } else {
            stableCount = 1
        }

        return if (stableCount >= 3) rawQuad else null
    }
}

private fun Quad.maxCornerDistanceTo(other: Quad): Float {
    return listOf(
        norm(topLeft, other.topLeft),
        norm(topRight, other.topRight),
        norm(bottomRight, other.bottomRight),
        norm(bottomLeft, other.bottomLeft),
    ).max().toFloat()
}

fun lerp(a: Point, b: Point, alpha: Float): Point {
    return Point(
        x = a.x + alpha * (b.x - a.x),
        y = a.y + alpha * (b.y - a.y)
    )
}

fun lerpQuad(a: Quad, b: Quad, alpha: Float): Quad {
    return Quad(
        topLeft = lerp(a.topLeft, b.topLeft, alpha),
        topRight = lerp(a.topRight, b.topRight, alpha),
        bottomRight = lerp(a.bottomRight, b.bottomRight, alpha),
        bottomLeft = lerp(a.bottomLeft, b.bottomLeft, alpha),
    )
}