package com.example.test.ui.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.test.R
import com.example.test.core.BaseActivity
import com.example.test.databinding.ActivityLoginBinding

class LoginActivity : BaseActivity<ActivityLoginBinding>(ActivityLoginBinding::inflate) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding.tvSingUp.click {
            openActivity(SingUpActivity::class.java)
        }

        binding.tvForgot.click {
            openActivity(ResetPassWordActivity::class.java)
        }

        binding.btnSingup.click {
            openActivity(SingUpActivity::class.java)
        }

        binding.btnLogin.click {
            openActivity(MainActivity::class.java)
        }
    }


}