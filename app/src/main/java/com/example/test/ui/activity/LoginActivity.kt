package com.example.test.ui.activity

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.InputType
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
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

        initView()

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

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        binding.edtPass.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableRight: Drawable? = binding.edtPass.compoundDrawables[2]
                if (drawableRight != null) {
                    val iconStart = binding.edtPass.width - binding.edtPass.paddingRight - drawableRight.bounds.width()
                    if (event.x >= iconStart) {
                        if ((binding.edtPass.inputType and InputType.TYPE_TEXT_VARIATION_PASSWORD) != 0) {
                            // Đang là Password -> chuyển sang hiện chữ
                            binding.edtPass.inputType =
                                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
                            binding.edtPass.transformationMethod = HideReturnsTransformationMethod.getInstance()
                            binding.edtPass.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_show_txt, 0)
                        } else {
                            // Đang hiện chữ -> chuyển về Password
                            binding.edtPass.inputType =
                                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                            binding.edtPass.transformationMethod = PasswordTransformationMethod.getInstance()
                            binding.edtPass.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_hide_txt, 0)
                        }
                        binding.edtPass.setSelection(binding.edtPass.length())
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }


}