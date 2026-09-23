package com.example.test.camera

import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.test.ImageSegmentationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.fairscan.imageprocessing.ColorMode
import org.fairscan.imageprocessing.ImageSize
import org.fairscan.imageprocessing.Mode
import org.fairscan.imageprocessing.OpticalMeasures
import org.fairscan.imageprocessing.Quad
import org.fairscan.imageprocessing.detectDocumentQuad
import org.fairscan.imageprocessing.extractDocument
import org.fairscan.imageprocessing.scaledTo
import org.opencv.android.Utils
import org.opencv.core.Mat

/**
 * Bản rút gọn: live analysis (segmentation + phát hiện quad) + chụp ảnh (ảnh gốc + ảnh đã
 * cắt/duỗi phẳng theo quad). Chưa có export PDF/JPEG hay danh sách nhiều trang — sẽ port sau.
 */
class CameraViewModel(
    private val imageSegmentationService: ImageSegmentationService,
) : ViewModel() {

    private val _liveAnalysisState = MutableStateFlow(LiveAnalysisState())
    val liveAnalysisState: StateFlow<LiveAnalysisState> = _liveAnalysisState.asStateFlow()
    private var quadStabilizer = QuadStabilizer()

    data class CapturedResult(val original: Bitmap, val cropped: Bitmap?, val quad: Quad?)

    private val _capturedResult = MutableStateFlow<CapturedResult?>(null)
    val capturedResult: StateFlow<CapturedResult?> = _capturedResult.asStateFlow()

    fun resetLiveAnalysis() {
        quadStabilizer = QuadStabilizer()
        _liveAnalysisState.value = LiveAnalysisState()
    }

    fun liveAnalysis(imageProxy: ImageProxy) {
        viewModelScope.launch {
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val result = withContext(Dispatchers.IO) {
                imageSegmentationService.runSegmentationAndReturn(imageProxy.toBitmap())
            }

            result?.let {
                val segmentation = result.segmentation
                val maskSize = segmentation.maskSize()
                val originalSize = ImageSize(imageProxy.width, imageProxy.height)
                val rawQuad = withContext(Dispatchers.Default) {
                    detectDocumentQuad(segmentation, originalSize, Mode.LIVE_ANALYSIS)
                        ?.rotate90(rotationDegrees / 90, maskSize)
                }
                val binaryMaskProvider = { ->
                    var binaryMask: Bitmap = segmentation.toBinaryMask()
                    if (rotationDegrees != 0) {
                        binaryMask = rotateBitmap(binaryMask, rotationDegrees.toFloat())
                    }
                    binaryMask
                }
                val stableQuad = quadStabilizer.update(rawQuad)
                _liveAnalysisState.value = LiveAnalysisState(
                    inferenceTime = result.inferenceTime,
                    binaryMaskProvider = binaryMaskProvider,
                    maskSize = maskSize,
                    stableQuad = stableQuad,
                )
            }

            imageProxy.close()
        }
    }

    /** Gọi từ CameraCaptureController.takePicture(...). */
    fun capture(imageProxy: ImageProxy, opticalMeasures: OpticalMeasures?) {
        viewModelScope.launch {
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val source = imageProxy.toBitmap()
            imageProxy.close()
            val original = if (rotationDegrees != 0) rotateBitmap(source, rotationDegrees.toFloat()) else source

            val cropped = withContext(Dispatchers.Default) {
                val segmentationResult = imageSegmentationService.runSegmentationAndReturn(original)
                val segmentation = segmentationResult?.segmentation
                val originalSize = ImageSize(original.width, original.height)
                val rawQuad = segmentation?.let { detectDocumentQuad(it, originalSize, Mode.CAPTURE) }
                // rawQuad ở hệ toạ độ của mask (ảnh output model), phải scale lên đúng
                // kích thước ảnh chụp thật trước khi đưa vào extractDocument().
                val scaledQuad = rawQuad?.let { quad ->
                    val maskSize = segmentation!!.maskSize()
                    quad.scaledTo(
                        fromWidth = maskSize.width,
                        fromHeight = maskSize.height,
                        toWidth = original.width.toDouble(),
                        toHeight = original.height.toDouble(),
                    )
                }
                Pair(scaledQuad?.let { extractDocumentBitmap(original, it, opticalMeasures) }, scaledQuad)
            }

            _capturedResult.value = CapturedResult(original, cropped.first, cropped.second)
        }
    }

    fun clearCapturedResult() {
        _capturedResult.value = null
    }

    private fun extractDocumentBitmap(
        source: Bitmap,
        quad: Quad,
        opticalMeasures: OpticalMeasures?,
    ): Bitmap {
        val inputMat = Mat()
        Utils.bitmapToMat(source, inputMat)
        val resultMat = extractDocument(
            inputMat = inputMat,
            quad = quad,
            rotationDegrees = 0, // source đã được xoay đúng chiều ở trên rồi
            colorMode = ColorMode.COLOR,
            maxPixels = 16_000_000L,
            opticalMeasures = opticalMeasures,
        )
        val resultBitmap = createBitmap(resultMat.cols(), resultMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(resultMat, resultBitmap)
        inputMat.release()
        resultMat.release()
        return resultBitmap
    }

    class Factory(
        private val imageSegmentationService: ImageSegmentationService,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CameraViewModel(imageSegmentationService) as T
        }
    }
}

fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(angle)
    return createBitmap(source, 0, 0, source.width, source.height, matrix, true)
}