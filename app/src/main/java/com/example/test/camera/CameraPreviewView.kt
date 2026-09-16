package com.example.test.camera

import android.content.Context
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import com.example.test.databinding.ViewCameraPreviewBinding
import org.fairscan.imageprocessing.CameraIntrinsics
import org.fairscan.imageprocessing.OpticalMeasures
import org.fairscan.imageprocessing.cameraIntrinsics
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.max

/**
 * View/XML thay thế cho @Composable CameraPreview + AnalysisOverlay.
 *
 * Cách dùng trong Fragment/Activity:
 *   binding.cameraPreviewView.onImageAnalyzed = { proxy -> viewModel.liveAnalysis(proxy) }
 *   binding.cameraPreviewView.onError = { msg, t -> viewModel.logError(msg, t) }
 *   // sau khi đã có quyền camera:
 *   binding.cameraPreviewView.start(viewLifecycleOwner)
 *   ...
 *   binding.cameraPreviewView.stop() // trong onDestroyView
 *
 *   // đổ dữ liệu overlay quad, ví dụ trong repeatOnLifecycle:
 *   viewModel.liveAnalysisState.collect { state ->
 *       binding.cameraPreviewView.overlayView.update(state, isDebugMode)
 *   }
 *
 * Yêu cầu quyền CAMERA phải được xin/kiểm tra ở nơi gọi (Activity/Fragment) trước khi
 * gọi start() — class này không tự request permission (khác với bản Compose cũ nhận
 * thẳng CameraPermissionState).
 */
class CameraPreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private val binding = ViewCameraPreviewBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    /** Gọi đúng 1 lần, ngay sau khi PreviewView nội bộ được tạo. */
    var onPreviewViewReady: (PreviewView) -> Unit = {}

    /** Gọi cho mỗi frame phân tích. Chạy trên background thread — không đụng View ở đây. */
    var onImageAnalyzed: (ImageProxy) -> Unit = {}

    /** Gọi khi bind use case camera thất bại. */
    var onError: (String, Throwable) -> Unit = { _, _ -> }

    val captureController = CameraCaptureController()
    val overlayView: QuadOverlayView get() = binding.overlayView

    var bindState: CameraBindState = CameraBindState.Idle
        private set

    private var lifecycleOwner: LifecycleOwner? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var analysisExecutor: ExecutorService? = null

    init {
        captureController.previewView = binding.previewView
        onPreviewViewReady(binding.previewView)
        binding.retryButton.setOnClickListener { bindCamera() }
    }

    /** Gọi khi đã có quyền camera và có sẵn LifecycleOwner để bind vào. */
    fun start(lifecycleOwner: LifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner
        analysisExecutor = Executors.newSingleThreadExecutor()
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener(
            {
                cameraProvider = future.get()
                bindCamera()
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    /** Gọi từ onDestroyView()/onDestroy() của Fragment/Activity. */
    fun stop() {
        cameraProvider?.unbindAll()
        analysisExecutor?.shutdown()
        analysisExecutor = null
        cameraProvider = null
        lifecycleOwner = null
    }

    private fun bindCamera() {
        val owner = lifecycleOwner ?: return
        val provider = cameraProvider ?: return
        val executor = analysisExecutor ?: return

        val result = runCatching {
            bindCameraUseCases(
                owner, provider, executor, binding.previewView,
                onImageAnalyzed, captureController
            )
        }
        bindState = result.fold(
            onSuccess = { CameraBindState.Bound },
            onFailure = {
                onError("Camera unavailable", it)
                CameraBindState.Error(it)
            }
        )
        showError(bindState is CameraBindState.Error)
    }

    private fun showError(show: Boolean) {
        binding.errorLayout.isVisible = show
        binding.previewView.isVisible = !show
        binding.overlayView.isVisible = !show
    }
}

@OptIn(ExperimentalCamera2Interop::class)
fun bindCameraUseCases(
    lifecycleOwner: LifecycleOwner,
    cameraProvider: ProcessCameraProvider,
    executor: ExecutorService,
    previewView: PreviewView,
    onImageAnalyzed: (ImageProxy) -> Unit,
    captureController: CameraCaptureController,
) {
    cameraProvider.unbindAll()

    val ratio_4_3 = ResolutionSelector.Builder()
        .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
        .build()
    val preview: Preview = Preview.Builder().setResolutionSelector(ratio_4_3).build()
    preview.surfaceProvider = previewView.surfaceProvider

    val cameraSelector: CameraSelector =
        CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
    val imageAnalysis = ImageAnalysis.Builder()
        .setResolutionSelector(ratio_4_3)
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888).build()
    imageAnalysis.setAnalyzer(executor, onImageAnalyzed)

    val imageCaptureBuilder = ImageCapture.Builder()
        .setResolutionSelector(
            ResolutionSelector.Builder()
                .setResolutionStrategy(
                    ResolutionStrategy(
                        Size(4400, 3300),
                        ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER
                    )
                )
                .setAspectRatioStrategy(
                    AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY
                )
                .build()
        )
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)

    Camera2Interop.Extender(imageCaptureBuilder)
        .setSessionCaptureCallback(object : CameraCaptureSession.CaptureCallback() {
            override fun onCaptureCompleted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                result: TotalCaptureResult
            ) {
                result.get(CaptureResult.LENS_FOCUS_DISTANCE)?.let {
                    captureController.lastFocusDistanceDiopters = it
                }
            }
        })

    val imageCapture = imageCaptureBuilder.build()
    captureController.imageCapture = imageCapture

    val camera = cameraProvider.bindToLifecycle(
        lifecycleOwner, cameraSelector, imageAnalysis, preview, imageCapture
    )
    captureController.cameraControl = camera.cameraControl
    captureController.setCameraCharacteristics(Camera2CameraInfo.from(camera.cameraInfo))
}

class CameraCaptureController {
    var cameraControl: CameraControl? = null
    var imageCapture: ImageCapture? = null
    private val executor = Executors.newSingleThreadExecutor()
    var previewView: PreviewView? = null
    var cameraIntrinsics: CameraIntrinsics? = null
    var canUseFocusDistance = false

    @Volatile
    var lastFocusDistanceDiopters: Float? = null

    fun shutdown() {
        executor.shutdown()
    }

    fun takePicture(onImageCaptured: (ImageProxy?, OpticalMeasures?) -> Unit) {
        imageCapture?.takePicture(
            executor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    val diopters = lastFocusDistanceDiopters
                    val subjectDistanceInMm =
                        if (canUseFocusDistance && diopters != null && diopters != 0.0f) {
                            1000 / diopters
                        } else {
                            null
                        }
                    onImageCaptured(
                        imageProxy,
                        cameraIntrinsics?.let { OpticalMeasures(it, subjectDistanceInMm) })
                }
                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraCapture", "Image capture failed: ${exception.message}", exception)
                    onImageCaptured(null, null)
                }
            }
        )
    }

    /** [x], [y]: toạ độ theo hệ toạ độ của chính PreviewView (vd. lấy từ onTouchEvent/ontap). */
    fun tapToFocus(x: Float, y: Float) {
        val view = previewView ?: return
        val control = cameraControl ?: return

        val factory = view.meteringPointFactory
        val point = factory.createPoint(x, y)

        val action = FocusMeteringAction.Builder(point)
            .setAutoCancelDuration(5, TimeUnit.SECONDS)
            .build()

        control.startFocusAndMetering(action)
    }

    @OptIn(ExperimentalCamera2Interop::class)
    fun setCameraCharacteristics(cameraInfo: Camera2CameraInfo) {
        val focalLengths = cameraInfo.getCameraCharacteristic(
            CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS
        )
        val sensorSize = cameraInfo.getCameraCharacteristic(
            CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE
        )
        cameraIntrinsics =
            if (focalLengths == null || focalLengths.size != 1 || sensorSize == null) {
                null
            } else {
                cameraIntrinsics(focalLengths[0], max(sensorSize.width, sensorSize.height))
            }
        val calibration = cameraInfo.getCameraCharacteristic(
            CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION
        )
        canUseFocusDistance =
            calibration == CameraMetadata.LENS_INFO_FOCUS_DISTANCE_CALIBRATION_CALIBRATED
                    || calibration == CameraMetadata.LENS_INFO_FOCUS_DISTANCE_CALIBRATION_APPROXIMATE
    }
}

sealed interface CameraBindState {
    object Idle : CameraBindState
    object Bound : CameraBindState
    data class Error(val throwable: Throwable) : CameraBindState
}