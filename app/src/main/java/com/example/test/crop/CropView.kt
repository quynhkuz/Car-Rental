package com.example.test.crop

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Size
import android.view.MotionEvent
import android.view.View
import org.fairscan.imageprocessing.Quad
import kotlin.compareTo

/**
 * View/XML thay thế cho CropScreen.kt (Compose): hiển thị ảnh + khung quad có thể kéo từng góc
 * + kính lúp (loupe) hiện khi đang kéo.
 *
 * Cách dùng:
 *   cropView.setImage(bitmap, initialQuad)
 *   // nút "Xác nhận":
 *   val editedQuad = cropView.getEditedQuad()
 *
 * Lưu ý: View không có "touch slop" tách biệt như Compose pointerInput, nên không cần
 * theo dõi 2 luồng chạm song song như bản gốc — touch-down được xử lý ngay, luôn chính xác.
 */
class CropView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    companion object {
        private const val ZOOM_FACTOR = 3f
    }

    private val quadHandler = QuadEditingHandler()
    val state = CropScreenState()

    private var bitmap: Bitmap? = null
    private var lastTouchPos: PointF? = null
    private var loupeVisible = false
    private var lastKnownFocusPosition: PointF? = null
    private val hideLoupeHandler = Handler(Looper.getMainLooper())

    private val density = resources.displayMetrics.density
    private val liftWiggleThresholdPx = 8f * density
    private val loupeRadiusPx = 60f * density
    private val loupeVerticalOffsetPx = 40f * density
    private val loupeScreenMarginPx = 8f * density
    private val loupeBorderWidthPx = 3f * density

    /** Đổi các màu này cho khớp theme của app. */
    var accentColor: Int = Color.parseColor("#6750A4")
        set(value) {
            field = value
            edgePaint.color = value
            handlePaint.color = value
            loupeBorderPaint.color = value
            loupeQuadLinePaint.color = value
            invalidate()
        }
    var loupeBackgroundColor: Int = Color.WHITE

    private val edgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = accentColor
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }
    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = accentColor
        alpha = 160
        style = Paint.Style.FILL
    }
    private val loupeBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = accentColor
        style = Paint.Style.STROKE
        strokeWidth = loupeBorderWidthPx
    }
    private val loupeQuadLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = accentColor
        alpha = 200
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    fun setImage(bitmap: Bitmap, initialQuad: Quad?) {
        this.bitmap = bitmap
        state.setInitialQuad(initialQuad)
        invalidate()
    }

    fun getEditedQuad(): Quad? = state.editableQuad

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bitmap = bitmap ?: return
        if (width == 0 || height == 0) return

        val containerSize = Size(width, height)
        state.containerSize = containerSize
        val displaySize = QuadCoordinateUtils.calculateDisplaySize(bitmap.width, bitmap.height, containerSize)
        val imageOffset = QuadCoordinateUtils.getImageOffset(containerSize, displaySize)

        val dstRect = Rect(
            imageOffset.width,
            imageOffset.height,
            imageOffset.width + displaySize.width,
            imageOffset.height + displaySize.height,
        )
        canvas.drawBitmap(bitmap, null, dstRect, bitmapPaint)

        state.editableQuad?.let { quad ->
            drawQuadOverlay(canvas, quad, containerSize, displaySize)
        }

        if (loupeVisible) {
            drawLoupe(canvas, bitmap, containerSize, displaySize)
        }
    }

    private fun drawQuadOverlay(canvas: Canvas, quad: Quad, containerSize: Size, displaySize: Size) {
        val corners = listOf(
            QuadCoordinateUtils.normalizedToScreen(quad.topLeft, containerSize, displaySize),
            QuadCoordinateUtils.normalizedToScreen(quad.topRight, containerSize, displaySize),
            QuadCoordinateUtils.normalizedToScreen(quad.bottomRight, containerSize, displaySize),
            QuadCoordinateUtils.normalizedToScreen(quad.bottomLeft, containerSize, displaySize),
        )
        for (i in corners.indices) {
            val a = corners[i]
            val b = corners[(i + 1) % corners.size]
            canvas.drawLine(a.x, a.y, b.x, b.y, edgePaint)
        }
        for (corner in corners) {
            canvas.drawCircle(corner.x, corner.y, QuadEditingHandler.CORNER_RADIUS, handlePaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val bitmap = bitmap ?: return false
        val containerSize = state.containerSize ?: return false
        val quad = state.editableQuad ?: return false
        val displaySize = QuadCoordinateUtils.calculateDisplaySize(bitmap.width, bitmap.height, containerSize)
        val pos = PointF(event.x, event.y)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val cornerIndex = quadHandler.findTouchedCorner(pos, quad, containerSize, displaySize)
                state.onTouchDown(pos, cornerIndex)
                showLoupe()
                if (cornerIndex >= 0) {
                    state.startCornerDrag(cornerIndex)
                }
                lastTouchPos = pos
                invalidate()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                state.dragPosition = pos
                if (state.isDragging()) {
                    val prevQuad = state.editableQuad!!
                    val prev = lastTouchPos ?: pos
                    val dx = pos.x - prev.x
                    val dy = pos.y - prev.y
                    state.recordDragStep(prevQuad, QuadCoordinateUtils.distance(prev, pos))
                    val normalizedDelta = QuadCoordinateUtils.screenDeltaToNormalized(dx, dy, displaySize)
                    state.updateQuad(
                        quadHandler.updateQuadCorner(
                            prevQuad, state.draggedCornerIndex, normalizedDelta.x, normalizedDelta.y
                        )
                    )
                }
                lastTouchPos = pos
                invalidate()
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                state.rollbackLastDragStepIfLikelyLiftWiggle(liftWiggleThresholdPx)
                state.endDrag()
                state.onTouchUp()
                scheduleHideLoupe()
                lastTouchPos = null
                invalidate()
                return true
            }
        }
        return false
    }

    private fun showLoupe() {
        hideLoupeHandler.removeCallbacksAndMessages(null)
        loupeVisible = true
    }

    private fun scheduleHideLoupe() {
        hideLoupeHandler.postDelayed({
            loupeVisible = false
            invalidate()
        }, 1_000)
    }

    private fun drawLoupe(canvas: Canvas, bitmap: Bitmap, containerSize: Size, displaySize: Size) {
        val dragPos = state.dragPosition ?: return
        val quad = state.editableQuad

        val activeCornerIndex = state.draggedCornerIndex.takeIf { it >= 0 }
            ?: state.touchDownCornerIndex.takeIf { it >= 0 }

        val focusPosition = if (quad != null && activeCornerIndex != null) {
            val corner = when (activeCornerIndex) {
                0 -> quad.topLeft
                1 -> quad.topRight
                2 -> quad.bottomRight
                3 -> quad.bottomLeft
                else -> null
            }
            corner?.let { QuadCoordinateUtils.normalizedToScreen(it, containerSize, displaySize) }
        } else null

        if (focusPosition != null) lastKnownFocusPosition = focusPosition
        val effectiveFocus = focusPosition ?: lastKnownFocusPosition ?: dragPos

        val loupeCenter = computeLoupeCenter(
            dragPos, loupeRadiusPx, loupeVerticalOffsetPx, loupeScreenMarginPx, containerSize.width.toFloat()
        )

        val imageOffset = QuadCoordinateUtils.getImageOffset(containerSize, displaySize)
        val bitmapX = ((effectiveFocus.x - imageOffset.width) / displaySize.width * bitmap.width)
            .coerceIn(0f, (bitmap.width - 1).toFloat())
        val bitmapY = ((effectiveFocus.y - imageOffset.height) / displaySize.height * bitmap.height)
            .coerceIn(0f, (bitmap.height - 1).toFloat())

        val bitmapRegionHalf = (bitmap.width / displaySize.width.toFloat()) * loupeRadiusPx / ZOOM_FACTOR
        val loupeDiameter = loupeRadiusPx * 2

        val saveCount = canvas.save()
        val clipPath = Path().apply {
            addCircle(loupeCenter.x, loupeCenter.y, loupeRadiusPx, Path.Direction.CW)
        }
        canvas.clipPath(clipPath)
        canvas.drawColor(loupeBackgroundColor)

        val srcLeft = (bitmapX - bitmapRegionHalf).toInt().coerceAtLeast(0)
        val srcTop = (bitmapY - bitmapRegionHalf).toInt().coerceAtLeast(0)
        val srcRight = (bitmapX + bitmapRegionHalf).toInt().coerceAtMost(bitmap.width)
        val srcBottom = (bitmapY + bitmapRegionHalf).toInt().coerceAtMost(bitmap.height)

        if (srcRight > srcLeft && srcBottom > srcTop) {
            val dstOffsetX = loupeCenter.x - loupeRadiusPx +
                    ((srcLeft - (bitmapX - bitmapRegionHalf)) / (2 * bitmapRegionHalf) * loupeDiameter)
            val dstOffsetY = loupeCenter.y - loupeRadiusPx +
                    ((srcTop - (bitmapY - bitmapRegionHalf)) / (2 * bitmapRegionHalf) * loupeDiameter)
            val dstWidth = (srcRight - srcLeft) / (2 * bitmapRegionHalf) * loupeDiameter
            val dstHeight = (srcBottom - srcTop) / (2 * bitmapRegionHalf) * loupeDiameter

            canvas.drawBitmap(
                bitmap,
                Rect(srcLeft, srcTop, srcRight, srcBottom),
                RectF(dstOffsetX, dstOffsetY, dstOffsetX + dstWidth, dstOffsetY + dstHeight),
                bitmapPaint,
            )
        }

        if (quad != null) {
            val bitmapOriginX = bitmapX - bitmapRegionHalf
            val bitmapOriginY = bitmapY - bitmapRegionHalf
            val scale = loupeDiameter / (2 * bitmapRegionHalf)

            fun normalizedToLoupe(nx: Double, ny: Double): PointF {
                val bx = (nx * bitmap.width).toFloat()
                val by = (ny * bitmap.height).toFloat()
                return PointF(
                    loupeCenter.x - loupeRadiusPx + (bx - bitmapOriginX) * scale,
                    loupeCenter.y - loupeRadiusPx + (by - bitmapOriginY) * scale,
                )
            }

            val loupeCorners = listOf(
                normalizedToLoupe(quad.topLeft.x, quad.topLeft.y),
                normalizedToLoupe(quad.topRight.x, quad.topRight.y),
                normalizedToLoupe(quad.bottomRight.x, quad.bottomRight.y),
                normalizedToLoupe(quad.bottomLeft.x, quad.bottomLeft.y),
            )
            for (i in loupeCorners.indices) {
                val a = loupeCorners[i]
                val b = loupeCorners[(i + 1) % loupeCorners.size]
                canvas.drawLine(a.x, a.y, b.x, b.y, loupeQuadLinePaint)
            }
        }

        canvas.restoreToCount(saveCount)

        canvas.drawCircle(
            loupeCenter.x, loupeCenter.y, loupeRadiusPx - loupeBorderWidthPx / 2, loupeBorderPaint
        )
    }

    /**
     * Ưu tiên: phía trên ngón tay > bên trái > bên phải (giống bản gốc).
     */
    private fun computeLoupeCenter(
        dragPosition: PointF,
        loupeRadius: Float,
        verticalOffset: Float,
        screenMargin: Float,
        containerWidth: Float,
    ): PointF {
        val aboveCenterY = dragPosition.y - verticalOffset - loupeRadius
        if (aboveCenterY - loupeRadius >= screenMargin) {
            val cx = dragPosition.x.coerceIn(screenMargin + loupeRadius, containerWidth - screenMargin - loupeRadius)
            return PointF(cx, aboveCenterY)
        }

        val leftCenterX = dragPosition.x - verticalOffset - loupeRadius
        if (leftCenterX - loupeRadius >= screenMargin) {
            val cy = dragPosition.y.coerceIn(screenMargin + loupeRadius, Float.MAX_VALUE)
            return PointF(leftCenterX, cy)
        }

        val rightCenterX = dragPosition.x + verticalOffset + loupeRadius
        val cx = rightCenterX.coerceAtMost(containerWidth - screenMargin - loupeRadius)
        val cy = dragPosition.y.coerceIn(screenMargin + loupeRadius, Float.MAX_VALUE)
        return PointF(cx, cy)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        hideLoupeHandler.removeCallbacksAndMessages(null)
    }
}