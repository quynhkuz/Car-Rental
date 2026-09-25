package com.example.test.ui.activity.upfile

import android.Manifest
import android.animation.ObjectAnimator
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.EditText
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.test.R
import com.example.test.core.BaseActivity
import com.example.test.databinding.ActivityUpLoadFileBinding
import com.example.test.model.driver.DriverDocumentRequest
import com.example.test.ui.csview.CsImageField
import com.example.test.utils.TempPhotoStore
import com.google.gson.Gson
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class UpLoadFileActivity : BaseActivity<ActivityUpLoadFileBinding>(ActivityUpLoadFileBinding::inflate) {

    companion object {
        const val EXTRA_RESULT = "extra_result"

        private const val STATE_CCCD_NO = "state_cccd_no"
        private const val STATE_GPLX_NO = "state_gplx_no"
        private const val STATE_GPLX_CLASS = "state_gplx_class"
        private const val STATE_GPLX_EXPIRY = "state_gplx_expiry"
        private const val STATE_FRONT = "state_front"
        private const val STATE_BACK = "state_back"
        private const val STATE_SELFIE = "state_selfie"

        private const val CCCD_LENGTH = 12
        private const val GPLX_LENGTH = 12

        private val DISPLAY_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        private val API_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        private val GPLX_CLASSES = arrayOf(
            "A1", "A2", "A3", "A4", "A5", "A6",
            "B", "B1", "B2", "B3", "B4", "B5", "B6",
            "C", "C1", "C2", "C3",
            "D", "D1", "D2", "D3", "D4", "D5", "D6",
            "E", "E1", "E2", "E4", "E5", "E6",
            "F", "F1", "F2", "F3", "F4", "F5", "F6"
        )
    }

    private var gplxExpiry: LocalDate? = null
    private var pendingField: CsImageField? = null
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
                    TempPhotoStore.copyToTemp(this@UpLoadFileActivity, uri)
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

        if (savedInstanceState == null) {
            TempPhotoStore.clear(this)
        }

        setupInputs()
        setupImageFields()
        binding.btnSubmit.click { submit() }
        restoreState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_CCCD_NO, binding.edtCccdNo.text.toString())
        outState.putString(STATE_GPLX_NO, binding.edtGplxNo.text.toString())
        outState.putString(STATE_GPLX_CLASS, binding.edtGplxClass.text.toString())
        outState.putString(STATE_GPLX_EXPIRY, gplxExpiry?.toString())
        outState.putString(STATE_FRONT, binding.imgFront.getImage())
        outState.putString(STATE_BACK, binding.imgBack.getImage())
        outState.putString(STATE_SELFIE, binding.imgSelfie.getImage())
    }

    private fun setupInputs() {
        binding.edtCccdNo.applyDigitsOnly(CCCD_LENGTH)
        binding.edtGplxNo.applyDigitsOnly(GPLX_LENGTH)
        binding.edtGplxClass.click { showClassPicker() }
        binding.edtGplxExpiry.click { showDatePicker() }
    }

    private fun setupImageFields() {
        setupImageField(binding.imgFront)
        setupImageField(binding.imgBack)
        setupImageField(binding.imgSelfie)
    }

    private fun setupImageField(field: CsImageField) {
        field.onPick = { askPhotoSource(field) }
        field.onRemove = { TempPhotoStore.delete(field.getImage()) }
    }

    private fun restoreState(state: Bundle?) {
        if (state == null) return
        binding.edtCccdNo.setText(state.getString(STATE_CCCD_NO).orEmpty())
        binding.edtGplxNo.setText(state.getString(STATE_GPLX_NO).orEmpty())
        binding.edtGplxClass.setText(state.getString(STATE_GPLX_CLASS).orEmpty())
        state.getString(STATE_GPLX_EXPIRY)
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?.let { setExpiry(it) }
        binding.imgFront.setImage(state.getString(STATE_FRONT))
        binding.imgBack.setImage(state.getString(STATE_BACK))
        binding.imgSelfie.setImage(state.getString(STATE_SELFIE))
    }

    // ---------------- Hạng GPLX ----------------

    private fun showClassPicker() {
        val checked = GPLX_CLASSES.indexOf(binding.edtGplxClass.text.toString())
        AlertDialog.Builder(this)
            .setTitle(R.string.gplx_class)
            .setSingleChoiceItems(GPLX_CLASSES, checked) { dialog, which ->
                binding.edtGplxClass.setText(GPLX_CLASSES[which])
                binding.edtGplxClass.error = null
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    // ---------------- Ngày hết hạn ----------------

    private fun showDatePicker() {
        val initial = gplxExpiry ?: LocalDate.now().plusYears(1)
        DatePickerDialog(
            this,
            { _, year, month, day -> setExpiry(LocalDate.of(year, month + 1, day)) },
            initial.year,
            initial.monthValue - 1,
            initial.dayOfMonth
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
        }.show()
    }

    private fun setExpiry(date: LocalDate) {
        gplxExpiry = date
        binding.edtGplxExpiry.setText(date.format(DISPLAY_DATE))
        binding.edtGplxExpiry.error = null
    }

    // ---------------- Ảnh ----------------

    private fun askPhotoSource(field: CsImageField) {
        AlertDialog.Builder(this)
            .setTitle(R.string.pick_photo_source)
            .setItems(
                arrayOf(
                    getString(R.string.take_photo),
                    getString(R.string.choose_from_gallery)
                )
            ) { _, which ->
                if (which == 0) requestCamera(field) else requestGallery(field)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun requestGallery(field: CsImageField) {
        pendingField = field
        pickImageLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    private fun requestCamera(field: CsImageField) {
        Dexter.withContext(this)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    openCamera(field)
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

    private fun openCamera(field: CsImageField) {
        val file = TempPhotoStore.createFile(this)
        tempCapture = file
        pendingField = field
        takePictureLauncher.launch(TempPhotoStore.uriForFile(this, file))
    }

    private fun attachPhoto(file: File) {
        val field = pendingField ?: return
        pendingField = null
        TempPhotoStore.delete(field.getImage())
        field.setImage(file.absolutePath)
    }

    // ---------------- Submit ----------------

    private fun submit() {
        if (!validate()) return

        val request = DriverDocumentRequest(
            cccdNo = binding.edtCccdNo.text.toString().trim(),
            gplxNo = binding.edtGplxNo.text.toString().trim(),
            gplxClass = binding.edtGplxClass.text.toString().trim(),
            gplxExpiry = gplxExpiry?.format(API_DATE).orEmpty(),
            frontUrl = binding.imgFront.getImage().ifEmpty { null },
            backUrl = binding.imgBack.getImage().ifEmpty { null },
            selfieUrl = binding.imgSelfie.getImage().ifEmpty { null }
        )

        // TODO: upload ảnh lên server để đổi đường dẫn tạm thành url, rồi gọi API
        val json = Gson().toJson(request)
        showToast(getString(R.string.success_submit))
        closeActivity(Bundle().apply { putString(EXTRA_RESULT, json) })
    }

    private fun validate(): Boolean {
        var valid = true

        if (!isValidNumber(binding.edtCccdNo, CCCD_LENGTH, R.string.err_cccd_no)) valid = false
        if (!isValidNumber(binding.edtGplxNo, GPLX_LENGTH, R.string.err_gplx_no)) valid = false

        if (binding.edtGplxClass.text.isNullOrBlank()) {
            binding.edtGplxClass.error = getString(R.string.err_gplx_class)
            valid = false
        }

        when {
            gplxExpiry == null -> {
                binding.edtGplxExpiry.error = getString(R.string.err_gplx_expiry)
                valid = false
            }

            gplxExpiry!!.isBefore(LocalDate.now()) -> {
                binding.edtGplxExpiry.error = getString(R.string.err_gplx_expiry_past)
                valid = false
            }
        }

        if (!binding.imgFront.hasImage()) {
            binding.imgFront.showError(getString(R.string.err_cccd_front))
            valid = false
        }
        if (!binding.imgBack.hasImage()) {
            binding.imgBack.showError(getString(R.string.err_cccd_back))
            valid = false
        }

        return valid
    }

    private fun isValidNumber(field: EditText, length: Int, errorRes: Int): Boolean {
        val value = field.text.toString().trim()
        if (value.length == length && value.all { it.isDigit() }) {
            field.error = null
            return true
        }
        field.error = getString(errorRes)
        return false
    }

    private fun EditText.applyDigitsOnly(maxLength: Int) {
        inputType = android.text.InputType.TYPE_CLASS_NUMBER
        filters = arrayOf(InputFilter.LengthFilter(maxLength))
    }

    private fun View.showError(message: String) {
        shake()
        showToast(message)
    }

    private fun View.shake() {
        animate().cancel()
        ObjectAnimator.ofFloat(this, View.TRANSLATION_X, 0f, 14f, -14f, 9f, -9f, 5f, -5f, 0f).apply {
            duration = 450
            interpolator = LinearInterpolator()
            start()
        }
    }
}
