package com.example.test.ui.activity.upfile

import android.Manifest
import android.os.Bundle
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.test.R
import com.example.test.core.BaseActivity
import com.example.test.databinding.ActivityUpLoadPhotoBinding
import com.example.test.utils.TempPhotoStore
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class UpLoadPhotoActivity : BaseActivity<ActivityUpLoadPhotoBinding>(ActivityUpLoadPhotoBinding::inflate) {

    companion object {
        private const val STATE_FILE_PHOTO = "state_file_photo"
        const val EXTRA_FILE_PHOTO = "extra_file_photo"
    }

    /** Đường dẫn tuyệt đối của ảnh đang chọn, nằm trong bộ nhớ tạm (cacheDir) của app. */
    var filePhoto = ""

    private var tempCapture: File? = null

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            val file = tempCapture
            tempCapture = null
            if (success && file != null && file.length() > 0L) {
                attachPhoto(file)
            } else {
                file?.delete()
                showToast(getString(R.string.err_photo_failed))
            }
        }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri == null) return@registerForActivityResult
            lifecycleScope.launch {
                val file = withContext(Dispatchers.IO) {
                    TempPhotoStore.copyToTemp(this@UpLoadPhotoActivity, uri)
                }
                if (file == null) {
                    showToast(getString(R.string.err_photo_failed))
                } else {
                    attachPhoto(file)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.icCamera.click { requestCamera() }
        binding.icGallery.click {
            pickImageLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
        binding.icRemove.click { removePhoto() }
        binding.btnUp.click { uploadPhoto() }

        if (savedInstanceState == null) {
            TempPhotoStore.clear(this)
        }
        filePhoto = savedInstanceState?.getString(STATE_FILE_PHOTO) ?: ""
        renderPhoto()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_FILE_PHOTO, filePhoto)
    }

    private fun requestCamera() {
        Dexter.withContext(this)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    openCamera()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    showToast(getString(R.string.err_camera_permission))
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            }).check()
    }

    private fun openCamera() {
        val file = TempPhotoStore.createFile(this)
        tempCapture = file
        takePictureLauncher.launch(TempPhotoStore.uriForFile(this, file))
    }

    private fun attachPhoto(file: File) {
        TempPhotoStore.delete(filePhoto)
        filePhoto = file.absolutePath
        renderPhoto()
    }

    private fun removePhoto() {
        TempPhotoStore.delete(filePhoto)
        filePhoto = ""
        renderPhoto()
    }

    private fun renderPhoto() {
        val hasPhoto = filePhoto.isNotEmpty() && File(filePhoto).exists()
        if (!hasPhoto) {
            filePhoto = ""
            binding.img.setImageDrawable(null)
        } else {
            Glide.with(this).load(filePhoto).into(binding.img)
        }
        binding.vImg.isVisible = hasPhoto
        binding.vUpload.isVisible = !hasPhoto
        binding.btnUp.isEnabled = hasPhoto
        binding.btnUp.alpha = if (hasPhoto) 1f else 0.5f
    }

    private fun uploadPhoto() {
        if (filePhoto.isEmpty()) {
            showToast(getString(R.string.err_photo_failed))
            return
        }
//        closeActivity(Bundle().apply { putString(EXTRA_FILE_PHOTO, filePhoto) })

        openActivity(UpLoadFileActivity::class.java)
    }
}
