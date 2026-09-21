package com.example.test.ui.fragment

import android.os.Bundle
import android.view.View
import com.example.test.core.BaseFragment
import com.example.test.databinding.FragmentProfileBinding


class ProfileFragment : BaseFragment<FragmentProfileBinding>(FragmentProfileBinding::inflate) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onInitView(view: View, savedInstanceState: Bundle?) {
        super.onInitView(view, savedInstanceState)
    }


    companion object {
        @JvmStatic
        fun newInstance() = ProfileFragment()
    }
}