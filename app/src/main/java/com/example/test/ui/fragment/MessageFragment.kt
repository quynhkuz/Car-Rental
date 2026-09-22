package com.example.test.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.test.R
import com.example.test.core.BaseFragment
import com.example.test.databinding.FragmentMessageBinding

class MessageFragment : BaseFragment<FragmentMessageBinding>(FragmentMessageBinding::inflate) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onInitView(view: View, savedInstanceState: Bundle?) {
        super.onInitView(view, savedInstanceState)

    }

    companion object {
        @JvmStatic
        fun newInstance() = MessageFragment()
    }
}