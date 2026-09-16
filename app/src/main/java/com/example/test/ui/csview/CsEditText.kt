package com.example.test.ui.csview

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt
import com.example.test.R

class CsEditText : AppCompatEditText {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )


    init {

        setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            resources.getDimension(com.intuit.sdp.R.dimen._11sdp)
        )

        typeface = ResourcesCompat.getFont(
            context,
            R.font.roboto_regular
        )

        setTextColor("#000000".toColorInt())
        setHintTextColor("#7F7F7F".toColorInt())

        setBackgroundResource(R.drawable.cs_bg_edt)

        val padding = resources.getDimensionPixelSize(
            com.intuit.sdp.R.dimen._13sdp
        )
        // Padding
        setPadding(padding, 0, padding, 0)

        gravity = Gravity.CENTER_VERTICAL

    }


}