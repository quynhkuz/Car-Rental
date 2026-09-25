package com.example.test.ui.csview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.test.R
import com.example.test.databinding.ViewCsImageFieldBinding

/**
 * Ô chọn ảnh: hiển thị placeholder (camera + nhãn) khi chưa có ảnh,
 * hiển thị thumbnail + nút xoá khi đã có ảnh.
 */
class CsImageField : FrameLayout {

    private val fieldBinding: ViewCsImageFieldBinding

    private var imagePath: String = ""

    /** Lời gọi lại khi người dùng bấm vào ô (kể cả khi đã có ảnh, để thay ảnh). */
    var onPick: (() -> Unit)? = null

    /** Lời gọi lại khi người dùng bấm nút xoá ảnh. */
    var onRemove: (() -> Unit)? = null

    constructor(context: Context) : super(context) {
        fieldBinding = ViewCsImageFieldBinding.inflate(LayoutInflater.from(context), this, true)
        initView(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        fieldBinding = ViewCsImageFieldBinding.inflate(LayoutInflater.from(context), this, true)
        initView(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        fieldBinding = ViewCsImageFieldBinding.inflate(LayoutInflater.from(context), this, true)
        initView(attrs)
    }

    private fun initView(attrs: AttributeSet?) {
        context.withStyledAttributes(attrs, R.styleable.CsImageField) {
            fieldBinding.tvLabel.text = getString(R.styleable.CsImageField_label) ?: ""
        }

        isClickable = true
        isFocusable = true

        setOnClickListener { onPick?.invoke() }
        fieldBinding.icRemove.setOnClickListener {
            setImage(null)
            onRemove?.invoke()
        }
    }

    /** @param path đường dẫn ảnh trong bộ nhớ tạm, null để xoá ảnh hiện tại. */
    fun setImage(path: String?) {
        imagePath = path.orEmpty()
        val hasImage = imagePath.isNotEmpty()
        fieldBinding.imgPhoto.isVisible = hasImage
        fieldBinding.icRemove.isVisible = hasImage
        fieldBinding.vPlaceholder.isVisible = !hasImage

        if (hasImage) {
            Glide.with(this).load(imagePath).into(fieldBinding.imgPhoto)
        } else {
            fieldBinding.imgPhoto.setImageDrawable(null)
        }
    }

    fun getImage(): String = imagePath

    fun hasImage(): Boolean = imagePath.isNotEmpty()

    fun setLabel(text: String) {
        fieldBinding.tvLabel.text = text
    }
}
