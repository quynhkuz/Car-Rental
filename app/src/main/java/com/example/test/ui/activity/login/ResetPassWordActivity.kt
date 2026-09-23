package com.example.test.ui.activity.login

import android.os.Bundle
import com.example.test.core.BaseActivity
import com.example.test.databinding.ActivityResetPassWordBinding

class ResetPassWordActivity : BaseActivity<ActivityResetPassWordBinding>(
    ActivityResetPassWordBinding::inflate) {


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