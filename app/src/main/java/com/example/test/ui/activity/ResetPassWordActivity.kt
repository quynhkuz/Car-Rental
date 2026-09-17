package com.example.test.ui.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.test.R
import com.example.test.core.BaseActivity
import com.example.test.databinding.ActivityResetPassWordBinding

class ResetPassWordActivity : BaseActivity<ActivityResetPassWordBinding>(ActivityResetPassWordBinding::inflate) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.tvReturn.click {
            closeActivity()
        }

        binding.tvSingUp.click {
            openActivity(SingUpActivity::class.java)
        }

    }


}