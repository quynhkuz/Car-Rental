package com.example.test.core

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<B : ViewBinding>(val bindingFactory: (LayoutInflater) -> B) :
    AppCompatActivity() {

    val binding: B by lazy { bindingFactory(layoutInflater) }

    val mySharedPre: MySharedPreferences by lazy { MySharedPreferences(this) }

    private var activityResultCallback: ((ActivityResult) -> Unit)? = null
    private val activityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            activityResultCallback?.invoke(result)
        }


    open fun binding() {
        setContentView(binding.root)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupEdgeToEdge(this)
        binding()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            setStatusBarTransparent(window, binding.root)
        } else {
            setStatusBarTransparent(window)
        }
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun View.hide() {
        visibility = View.GONE
    }

    fun View.show() {
        visibility = View.VISIBLE
    }

    fun View.invisible() {
        visibility = View.INVISIBLE
    }

    fun View.click(action: (view: View) -> Unit) {
        setOnClickListener { action(it) }
    }


    open fun openActivity(
        destination: Class<*>,
        canBack: Boolean = true,
        bundle: Bundle? = null
    ) {
        val intent = Intent(this, destination)
        if (bundle != null) {
            intent.putExtras(bundle)
        }
        startActivity(intent)
//        this.overridePendingTransition(
//            R.anim.enter_from_right, R.anim.exit_to_left
//        )
        if (!canBack) {
            finish()
        }
    }

    open fun closeActivity(bundle: Bundle? = null) {
        if (bundle != null) {
            setResult(RESULT_OK, Intent().apply {
                putExtras(bundle)
            })
        }
        finish()
//        this.overridePendingTransition(
//            R.anim.enter_from_left, R.anim.exit_to_right
//        )
    }


    open fun openActivityCallBack(
        destination: Class<*>,
        bundle: Bundle? = null,
        onCallBack: (ActivityResult) -> Unit
    ) {
        activityResultCallback = onCallBack
        val intent = Intent(this, destination).apply {
            bundle?.let { putExtras(it) }
        }
        activityLauncher.launch(intent)
    }


    open fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun setStatusBarTransparent(window: Window, rootView: View) {
        // Edge-to-edge tổng thể (status bar trong suốt, tràn viền)
        //true -- tự động chừa khoảng trống cho status bar và navigation bar
//        WindowCompat.setDecorFitsSystemWindows(window, true)
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)

        // Ẩn status bar
//        insetsController.hide(WindowInsetsCompat.Type.statusBars())

//        // Ẩn thanh điều hướng, kiểu vuốt để hiện tạm rồi tự ẩn lại (thay cho IMMERSIVE_STICKY)
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.navigationBars())

        // Đảm bảo navigation bar được hiển thị (không ẩn)
//        insetsController.show(WindowInsetsCompat.Type.navigationBars())

        // Icon status bar tối màu (true)
        insetsController.isAppearanceLightStatusBars = true

        // Bù padding để content không bị navigation bar đè lên
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val navBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())

            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
//                statusBarInsets.top,      // giữ nguyên nếu bạn muốn status bar vẫn tràn, đổi thành 0 nếu không cần padding top
                view.paddingRight,
                view.paddingBottom
//                navBarInsets.bottom       // đẩy nội dung lên trên, tránh bị nav bar che
            )
            insets
        }
    }

    @Suppress("DEPRECATION")
    fun setStatusBarTransparent(window: Window) {
        val flags = (
                //Giữ giao diện ổn định, không bị thay đổi khi ẩn/hiện thanh trạng thái hoặc thanh điều hướng
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        //Cho phép layout nằm bên dưới thanh trạng thái.
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        //Cho phép layout nằm bên dưới thanh điều hướng.
//                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        // Ẩn thanh điều hướng.
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        // Ẩn thanh trạng thái.
//                or View.SYSTEM_UI_FLAG_FULLSCREEN
                        //khi người dùng vuốt để hiện lại thanh trạng thái/thanh điều hướng, chúng sẽ tự động ẩn sau vài giây.
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        window.decorView.systemUiVisibility = flags
        //Đặt màu trong suốt cho status bar và navigation bar:
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        val insetsController = WindowInsetsControllerCompat(
            window,
            window.decorView
        )
        insetsController.isAppearanceLightStatusBars = true
    }

    fun setupEdgeToEdge(activity: ComponentActivity) {
        activity.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, // màu khi light mode
                Color.TRANSPARENT  // màu khi dark mode
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.parseColor("#FFFFFF"), // màu nền nav bar (light mode) — đổi theo ý bạn
                Color.parseColor("#FFFFFF")  // màu nền nav bar (dark mode)
            )
        )
    }


}