package com.example.test

import android.Manifest
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.test.databinding.ActivityMainBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.fairscan.imageprocessing.Quad

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private val imageSegmentationService by lazy { ImageSegmentationService(applicationContext) }
    private val cameraViewModel: CameraViewModel by lazy {
        ViewModelProvider(this, CameraViewModel.Factory(imageSegmentationService))[CameraViewModel::class.java]
    }
    private var isDebugMode = false
    private var isTorchEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            imageSegmentationService.initialize()
        }

        binding.cameraPreviewView.onImageAnalyzed = { imageProxy ->
            cameraViewModel.liveAnalysis(imageProxy)
        }
        binding.cameraPreviewView.onError = { message, _ ->
            runOnUiThread {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }

        binding.flashButton.setOnClickListener {
            isTorchEnabled = !isTorchEnabled
            binding.cameraPreviewView.captureController.cameraControl?.enableTorch(isTorchEnabled)
            binding.flashButton.text = if (isTorchEnabled) "Đèn: Bật" else "Đèn: Tắt"
        }

        binding.captureButton.setOnClickListener {
            binding.cameraPreviewView.captureController.takePicture { imageProxy, opticalMeasures ->
                if (imageProxy != null) {
                    cameraViewModel.capture(imageProxy, opticalMeasures)
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Chụp ảnh thất bại", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                cameraViewModel.liveAnalysisState.collect { state ->
                    binding.cameraPreviewView.overlayView.update(state, isDebugMode)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                cameraViewModel.capturedResult.collect { result ->
                    if (result != null) {
                        val timestamp = System.currentTimeMillis()
                        val originalUri = saveBitmap(result.original, "scan_${timestamp}_original")
                        val croppedUri = result.cropped?.let {
                            saveBitmap(it, "scan_${timestamp}_cropped")
                        }
                        if (originalUri != null) {
                            onImagesCaptured(originalUri, croppedUri, result.quad)
                        } else {
                            Toast.makeText(this@MainActivity, "Lưu ảnh thất bại", Toast.LENGTH_SHORT).show()
                        }
                        cameraViewModel.clearCapturedResult()
                    }
                }
            }
        }

        Dexter.withContext(this)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    binding.cameraPreviewView.start(this@MainActivity)
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    Toast.makeText(
                        this@MainActivity,
                        "Cần quyền camera để sử dụng tính năng này",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            }).check()
    }

    /**
     * Nơi nhận đường dẫn (content Uri) của 2 ảnh sau khi chụp — [originalUri] luôn có,
     * [croppedUri] null nếu không phát hiện được văn bản trong ảnh.
     * Đổi nội dung hàm này tuỳ ý: mở Activity khác kèm 2 Uri, log ra, gọi API upload, v.v.
     */
    private fun onImagesCaptured(originalPath: String, croppedPath: String?, quad: Quad?) {
        android.util.Log.i("MainActivity", "originalPath=$originalPath croppedPath=$croppedPath")

        val message = if (croppedPath != null) {
            "Ảnh gốc: $originalPath\nẢnh cắt: $croppedPath"
        } else {
            "Ảnh gốc: $originalPath\n(không phát hiện được văn bản để cắt)"
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        openActivity(PreviewActivity::class.java, bundle = Bundle().apply {
            putString("key1",croppedPath)
            putString("key2",originalPath)
            quad?.let {
                putDoubleArray(
                    "key3",
                    doubleArrayOf(
                        it.topLeft.x, it.topLeft.y,
                        it.topRight.x, it.topRight.y,
                        it.bottomRight.x, it.bottomRight.y,
                        it.bottomLeft.x, it.bottomLeft.y,
                    )
                )
            }
        })

        // Ví dụ mở màn hình khác kèm 2 đường dẫn:
        // val intent = Intent(this, ResultActivity::class.java).apply {
        //     putExtra("original_path", originalPath)
        //     croppedPath?.let { putExtra("cropped_path", it) }
        // }
        // startActivity(intent)
    }

    /** Lưu vào bộ nhớ cache của app (cacheDir) — hệ thống có thể tự xoá khi thiếu dung lượng. */
    private fun saveBitmap(bitmap: Bitmap, name: String): String? {
        val dir = java.io.File(cacheDir, "Test").apply { mkdirs() }
        val file = java.io.File(dir, "$name.jpg")
        return try {
            java.io.FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            file.absolutePath
        } catch (e: java.io.IOException) {
            android.util.Log.e("MainActivity", "Lưu ảnh thất bại", e)
            null
        }
    }


//    private fun saveBitmap(bitmap: Bitmap, name: String): android.net.Uri? {
//        val values = ContentValues().apply {
//            put(MediaStore.Images.Media.DISPLAY_NAME, name)
//            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
//            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Test")
//        }
//        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null
//        contentResolver.openOutputStream(uri)?.use { out ->
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
//        }
//        return uri
//    }


    override fun onDestroy() {
        binding.cameraPreviewView.stop()
        super.onDestroy()
    }
}