package com.example.test.ui.activity

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.InputType
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.MotionEvent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.test.R
import com.example.test.core.BaseActivity
import com.example.test.databinding.ActivityLoginBinding
import com.example.test.model.LoginRequest
import com.example.test.viewmodel.CarViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.log


@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding>(ActivityLoginBinding::inflate) {


    val viewModel : CarViewModel by lazy { ViewModelProvider(this)[CarViewModel::class.java] }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
        observerData()

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
            val tk = binding.edtTk.text.toString()
            val mk = binding.edtPass.text.toString()

//            if(tk.isNotEmpty() || mk.isNotEmpty()){
//                viewModel.login(LoginRequest(tk,mk))
//            }else{
//                showToast(getString(R.string.the_account_and_password_fields_must_not_be_left_blank))
//            }


            openActivity(MainActivity::class.java)
        }
    }

    private fun observerData() {
        viewModel.loginResponse.observe(this){result ->
            if(result.isSuccess){
                Log.e("AAA","Data " + result.getOrNull())
            }
            else{
                showToast(getString(R.string.error))
            }
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