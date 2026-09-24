package com.example.test

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.test.crop.CropView
import com.example.test.databinding.ActivityCropBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.fairscan.imageprocessing.ColorMode
import org.fairscan.imageprocessing.Point
import org.fairscan.imageprocessing.Quad
import org.fairscan.imageprocessing.extractDocument
import org.opencv.android.Utils
import org.opencv.core.Mat

/**
 * Ví dụ dùng CropView: nhận đường dẫn ảnh gốc (String) + quad ban đầu (8 số double),
 * cho người dùng kéo chỉnh 4 góc, bấm Xác nhận -> chạy lại extractDocument()
 * với quad đã chỉnh -> lưu ảnh kết quả, trả về đường dẫn ảnh đã cắt.
 */
class CropActivity : BaseActivity<ActivityCropBinding>(ActivityCropBinding::inflate) {

    companion object {
        const val EXTRA_IMAGE_PATH = "image_path"
        const val EXTRA_QUAD = "quad" // DoubleArray[8]: tl.x,tl.y,tr.x,tr.y,br.x,br.y,bl.x,bl.y

        fun start(context: Context, imagePath: String, quad: Quad) {
            val intent = Intent(context, CropActivity::class.java).apply {
                putExtra(EXTRA_IMAGE_PATH, imagePath)
                putExtra(
                    EXTRA_QUAD,
                    doubleArrayOf(
                        quad.topLeft.x, quad.topLeft.y,
                        quad.topRight.x, quad.topRight.y,
                        quad.bottomRight.x, quad.bottomRight.y,
                        quad.bottomLeft.x, quad.bottomLeft.y,
                    )
                )
            }
            context.startActivity(intent)
        }
    }


    private var sourceBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH)
        val quadArray = intent.getDoubleArrayExtra(EXTRA_QUAD)

        if (imagePath == null || quadArray == null || quadArray.size != 8) {
            Toast.makeText(this, "Thiếu ảnh hoặc quad", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val quad = Quad(
            topLeft = Point(quadArray[0], quadArray[1]),
            topRight = Point(quadArray[2], quadArray[3]),
            bottomRight = Point(quadArray[4], quadArray[5]),
            bottomLeft = Point(quadArray[6], quadArray[7]),
        )

        sourceBitmap = BitmapFactory.decodeFile(imagePath)
        val bitmap = sourceBitmap
        if (bitmap == null) {
            Toast.makeText(this, "Không đọc được ảnh", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        binding.cropView.setImage(bitmap, quad)

        binding.cancelButton.setOnClickListener {
            closeActivity()
        }

        binding.confirmButton.setOnClickListener {
            val editedQuad = binding.cropView.getEditedQuad()
            val source = sourceBitmap
            if (editedQuad == null || source == null) {
                finish()
                return@setOnClickListener
            }
            lifecycleScope.launch(Dispatchers.Default) {
                val cropped = extractDocumentBitmap(source, editedQuad)
                val path = saveBitmap(cropped, "scan_${System.currentTimeMillis()}_cropped")

                runOnUiThread {
                    Toast.makeText(
                        this@CropActivity,
                        if (path != null) "Đã lưu ảnh đã cắt: $path" else "Lưu ảnh thất bại",
                        Toast.LENGTH_SHORT
                    ).show()
//                    finish()


                    closeActivity(bundle = Bundle().apply {
                        putString("key_data",path)
                    })
                }
            }
        }
    }

    private fun extractDocumentBitmap(source: Bitmap, quad: Quad): Bitmap {
        val inputMat = Mat()
        Utils.bitmapToMat(source, inputMat)
        val resultMat = extractDocument(
            inputMat = inputMat,
            quad = quad,
            rotationDegrees = 0,
            colorMode = ColorMode.COLOR,
            maxPixels = 16_000_000L,
        )
        val resultBitmap = Bitmap.createBitmap(resultMat.cols(), resultMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(resultMat, resultBitmap)
        inputMat.release()
        resultMat.release()
        return resultBitmap
    }

    /** Lưu vào bộ nhớ cache của app (cacheDir), trả về đường dẫn tuyệt đối. */
    private fun saveBitmap(bitmap: Bitmap, name: String): String? {
        val dir = java.io.File(cacheDir, "Test").apply { mkdirs() }
        val file = java.io.File(dir, "$name.jpg")
        return try {
            java.io.FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            file.absolutePath
        } catch (e: java.io.IOException) {
            android.util.Log.e("CropActivity", "Lưu ảnh thất bại", e)
            null
        }
    }
}