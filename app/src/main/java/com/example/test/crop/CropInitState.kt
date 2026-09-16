package com.example.test.crop

import android.graphics.Bitmap
import android.graphics.PointF
import android.util.Size
import org.fairscan.imageprocessing.Quad

sealed interface CropInitState {
    object Loading : CropInitState
    object Error : CropInitState
    data class Ready(
        val pageId: String,
        val bitmap: Bitmap,
        val quad: Quad
    ) : CropInitState
}

/**
 * Bản plain-Kotlin của CropScreenState gốc (Compose). Vì View vẽ theo kiểu imperative
 * (invalidate() để vẽ lại), không cần mutableStateOf — CropView tự gọi invalidate()
 * sau mỗi thay đổi.
 */
class CropScreenState {
    companion object {
        const val LIFT_WIGGLE_WINDOW_MS = 70L
    }

    var containerSize: Size? = null
    var editableQuad: Quad? = null
        private set
    var draggedCornerIndex: Int = -1
    var dragPosition: PointF? = null
    /** True từ lúc chạm vào 1 handle tới lúc nhấc tay. */
    var isTouching: Boolean = false
    /** Corner được phát hiện ngay tại touch-down thô (View không có touch-slop nên luôn chính xác). */
    var touchDownCornerIndex: Int = -1

    private var quadBeforeLastDragStep: Quad? = null
    private var lastDragStepDistancePx: Float = Float.MAX_VALUE
    private var lastDragStepAtMs: Long = 0L

    fun setInitialQuad(quad: Quad?) {
        editableQuad = quad
    }

    fun updateQuad(newQuad: Quad) {
        editableQuad = newQuad
    }

    fun startCornerDrag(cornerIndex: Int) {
        draggedCornerIndex = cornerIndex
        clearLastDragStep()
    }

    fun recordDragStep(previousQuad: Quad, dragDistancePx: Float, eventTimeMs: Long = System.currentTimeMillis()) {
        quadBeforeLastDragStep = previousQuad
        lastDragStepDistancePx = dragDistancePx
        lastDragStepAtMs = eventTimeMs
    }

    fun rollbackLastDragStepIfLikelyLiftWiggle(maxDistancePx: Float, nowMs: Long = System.currentTimeMillis()) {
        if (quadBeforeLastDragStep == null) return
        val isRecent = nowMs - lastDragStepAtMs <= LIFT_WIGGLE_WINDOW_MS
        val isSmall = lastDragStepDistancePx <= maxDistancePx
        if (isRecent && isSmall) {
            editableQuad = quadBeforeLastDragStep
        }
    }

    fun endDrag() {
        clearLastDragStep()
        draggedCornerIndex = -1
        // dragPosition giữ nguyên để loupe còn vẽ được trong 1s fade-out sau khi nhấc tay.
    }

    private fun clearLastDragStep() {
        quadBeforeLastDragStep = null
        lastDragStepDistancePx = Float.MAX_VALUE
        lastDragStepAtMs = 0L
    }

    fun onTouchDown(position: PointF, cornerIndex: Int = -1) {
        isTouching = true
        dragPosition = position
        touchDownCornerIndex = cornerIndex
    }

    fun onTouchUp() {
        isTouching = false
        touchDownCornerIndex = -1
    }

    fun isDragging(): Boolean = draggedCornerIndex >= 0
}