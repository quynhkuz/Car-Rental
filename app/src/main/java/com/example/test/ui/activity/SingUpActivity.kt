package com.example.test.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.test.R
import com.example.test.core.BaseActivity
import com.example.test.databinding.ActivitySingUpBinding

class SingUpActivity : BaseActivity<ActivitySingUpBinding>(ActivitySingUpBinding::inflate) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.tvLogin.click {
            openActivity(LoginActivity::class.java)
        }

        binding.btnLogin.click {
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        }


    }


}