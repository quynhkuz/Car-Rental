package com.example.test.ui.csview

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.toColorInt
import com.example.test.R

class CsButton : AppCompatButton {


    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){
        initView(attrs)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(attrs)
    }

    var type = 0

    fun initView(attrs: AttributeSet?) {
        context.withStyledAttributes(
            attrs,
            R.styleable.CsButton
        ) {
            type = getInt(R.styleable.CsButton_typeColor, 0)
        }
        backgroundTintList = null
        setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            resources.getDimension(com.intuit.sdp.R.dimen._13sdp)
        )

        typeface = ResourcesCompat.getFont(
            context,
            R.font.roboto_bold
        )

        isAllCaps = false

        if(type == 0){
            setBackgroundResource(R.drawable.cs_btn_black)
            setTextColor("#FFFFFF".toColorInt())
        }
        else{
            setBackgroundResource(R.drawable.cs_btn_gray)
            setTextColor("#000000".toColorInt())
        }


    }


}