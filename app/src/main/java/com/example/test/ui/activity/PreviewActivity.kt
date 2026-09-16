package com.example.test.ui.activity

import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.test.ui.activity.CropActivity.Companion.EXTRA_QUAD
import com.example.test.core.BaseActivity
import com.example.test.databinding.ActivityPreviewBinding
import org.fairscan.imageprocessing.Point
import org.fairscan.imageprocessing.Quad

class PreviewActivity : BaseActivity<ActivityPreviewBinding>(ActivityPreviewBinding::inflate) {

    var imgCrop: String = ""
    var imgOriginal= ""
    private var quad: Quad? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.extras?.let {
            imgCrop = it.getString("key1").toString()
            imgOriginal = it.getString("key2").toString()

            Glide.with(this).load(imgCrop).into(binding.imgCrop)
            Glide.with(this).load(imgOriginal).into(binding.imgOrigin)

            val quadArray = it.getDoubleArray("key3")
            quad = if (quadArray != null && quadArray.size == 8) {
                Quad(
                    topLeft = Point(quadArray[0], quadArray[1]),
                    topRight = Point(quadArray[2], quadArray[3]),
                    bottomRight = Point(quadArray[4], quadArray[5]),
                    bottomLeft = Point(quadArray[6], quadArray[7]),
                )
            } else null

        }


        binding.btnCrop.setOnClickListener {
//            val q = quad
//            if (imgOriginal.isNotEmpty() && q != null) {
//                CropActivity.start(this, imgOriginal, q)
//            } else {
//                android.widget.Toast.makeText(
//                    this, "Không có quad để chỉnh sửa", android.widget.Toast.LENGTH_SHORT
//                ).show()
//            }


            if (imgOriginal.isNotEmpty() && quad != null) {
                openActivityCallBack(CropActivity::class.java, bundle = Bundle().apply {
                    putString(CropActivity.EXTRA_IMAGE_PATH, imgOriginal)
                    putDoubleArray(
                        EXTRA_QUAD,
                        doubleArrayOf(
                            quad!!.topLeft.x, quad!!.topLeft.y,
                            quad!!.topRight.x, quad!!.topRight.y,
                            quad!!.bottomRight.x, quad!!.bottomRight.y,
                            quad!!.bottomLeft.x, quad!!.bottomLeft.y,
                        )
                    )
                }, onCallBack = {result ->
                    if (result.resultCode == RESULT_OK){
                        val f = result.data?.extras?.getString("key_data")
                        if(f != null){
                            imgCrop = f
                            Glide.with(this).load(imgCrop).into(binding.imgCrop)
                        }
                    }
                })
            } else {
                Toast.makeText(
                    this, "Không có quad để chỉnh sửa", Toast.LENGTH_SHORT
                ).show()
            }


        }

    }

}