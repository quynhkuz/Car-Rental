package com.example.test.ui.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.test.R
import com.example.test.core.BaseActivity
import com.example.test.databinding.ActivityCarDetailsBinding
import com.example.test.utils.setRoundedBackground

class CarDetailsActivity : BaseActivity<ActivityCarDetailsBinding>(ActivityCarDetailsBinding::inflate) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()

        binding.icBack.click {
            closeActivity()
        }

    }

    private fun initView() {
        binding.vCapacity.setRoundedBackground("#EDEDED".toColorInt(),10f)
        binding.vEngine.setRoundedBackground("#EDEDED".toColorInt(),10f)
        binding.vSpeed.setRoundedBackground("#EDEDED".toColorInt(),10f)
        binding.vAdvance.setRoundedBackground("#EDEDED".toColorInt(),10f)
        binding.vCharge.setRoundedBackground("#EDEDED".toColorInt(),10f)
        binding.vParking.setRoundedBackground("#EDEDED".toColorInt(),10f)
    }
}