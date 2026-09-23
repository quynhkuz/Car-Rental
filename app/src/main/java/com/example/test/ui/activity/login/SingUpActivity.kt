package com.example.test.ui.activity.login

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.InputType
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import com.example.test.R
import com.example.test.core.BaseActivity
import com.example.test.databinding.ActivitySingUpBinding

class SingUpActivity : BaseActivity<ActivitySingUpBinding>(ActivitySingUpBinding::inflate) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()

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


    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        binding.edtPassWord.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableRight: Drawable? = binding.edtPassWord.compoundDrawables[2]
                if (drawableRight != null) {
                    val iconStart = binding.edtPassWord.width - binding.edtPassWord.paddingRight - drawableRight.bounds.width()
                    if (event.x >= iconStart) {
                        if ((binding.edtPassWord.inputType and InputType.TYPE_TEXT_VARIATION_PASSWORD) != 0) {
                            // Đang là Password -> chuyển sang hiện chữ
                            binding.edtPassWord.inputType =
                                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
                            binding.edtPassWord.transformationMethod = HideReturnsTransformationMethod.getInstance()
                            binding.edtPassWord.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_show_txt, 0)
                        } else {
                            // Đang hiện chữ -> chuyển về Password
                            binding.edtPassWord.inputType =
                                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                            binding.edtPassWord.transformationMethod = PasswordTransformationMethod.getInstance()
                            binding.edtPassWord.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_hide_txt, 0)
                        }
                        binding.edtPassWord.setSelection(binding.edtPassWord.length())
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }


}