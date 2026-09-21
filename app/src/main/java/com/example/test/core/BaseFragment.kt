package com.example.test.core

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<B : ViewBinding>(
    private val bindingFactory: (LayoutInflater, ViewGroup?, Boolean) -> B
) : Fragment() {

    private var _binding: B? = null
    val binding: B
        get() = _binding ?: throw IllegalStateException(
            "Fragment binding chỉ dùng được trong onCreateView -> onDestroyView"
        )

    val mySharedPre: MySharedPreferences by lazy { MySharedPreferences(requireContext()) }

    private var activityResultCallback: ((ActivityResult) -> Unit)? = null
    private val activityLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            activityResultCallback?.invoke(result)
        }

    open fun onInitView(view: View, savedInstanceState: Bundle?) {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = bindingFactory(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onInitView(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun View.hide() { visibility = View.GONE }
    fun View.show() { visibility = View.VISIBLE }
    fun View.invisible() { visibility = View.INVISIBLE }
    fun View.click(action: (view: View) -> Unit) = setOnClickListener { action(it) }


    open fun openActivity(
        destination: Class<*>,
        canBack: Boolean = true,
        bundle: Bundle? = null
    ) {
        val intent = Intent(requireContext(), destination)
        bundle?.let { intent.putExtras(it) }
        startActivity(intent)
        if (!canBack) activity?.finish()
    }

    open fun openActivityCallBack(
        destination: Class<*>,
        bundle: Bundle? = null,
        onCallBack: (ActivityResult) -> Unit
    ) {
        activityResultCallback = onCallBack
        val intent = Intent(requireContext(), destination).apply {
            bundle?.let { putExtras(it) }
        }
        activityLauncher.launch(intent)
    }

    open fun closeActivity(bundle: Bundle? = null) {
        val act = activity ?: return
        if (bundle != null) {
            act.setResult(Activity.RESULT_OK, Intent().putExtras(bundle))
        }
        act.finish()
    }

    fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    open fun setFragmentResult(bundle: Bundle, requestKey: String = "request") {
        parentFragmentManager.setFragmentResult(requestKey, bundle)
    }


}

