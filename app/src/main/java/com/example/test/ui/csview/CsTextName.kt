package com.example.test.ui.csview

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt
import androidx.core.widget.TextViewCompat
import com.example.test.R

class CsTextName : AppCompatTextView {

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
            resources.getDimension(com.intuit.sdp.R.dimen._13sdp)
        )

        typeface = ResourcesCompat.getFont(
            context,
            R.font.roboto_bold
        )

        setTextColor("#000000".toColorInt())

        gravity = Gravity.CENTER_VERTICAL

    }


}