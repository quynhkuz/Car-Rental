package com.example.test.ui.csview

import android.content.Context
import android.util.AttributeSet
import android.view.View

class StatusBarView : View {

    private var statusBarHeight = 0

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        val resourceId = context.resources.getIdentifier(
            "status_bar_height", "dimen", "android"
        )
        if (resourceId > 0) {
            statusBarHeight = context.resources.getDimensionPixelSize(resourceId)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(width, statusBarHeight)
//        setMeasuredDimension(width, (statusBarHeight).toInt())
    }

}