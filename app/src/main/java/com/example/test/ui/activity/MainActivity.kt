package com.example.test.ui.activity

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.core.graphics.toColorInt
import com.example.test.core.BaseActivity
import com.example.test.databinding.ActivityMainBinding
import com.example.test.ui.fragment.HomeFragment
import com.example.test.ui.fragment.MessageFragment
import com.example.test.ui.fragment.ProfileFragment
import com.example.test.ui.fragment.SearchFragment
import com.example.test.utils.TYPE_HOME
import com.example.test.utils.TYPE_MESSAGE
import com.example.test.utils.TYPE_SEARCH
import com.example.test.utils.TYPE_PROFILE
import java.text.MessageFormat

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {


    var fgHome : HomeFragment? = null
    var fgSearch : SearchFragment? = null
    var fgProfile : ProfileFragment? = null
    var fgMessage : MessageFragment? = null

    var type = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()

        binding.icHome.click {
            openHome()
        }

        binding.icSearch.click { openSearch() }

        binding.icProfile.click {
            openProfile()
        }

        binding.icMessage.click {
            openMessage()
        }

    }

    fun openMessage() {
        if (type != TYPE_MESSAGE)
        {
            restViewNav()
            type = TYPE_MESSAGE
            binding.icMessage.imageTintList =
                ColorStateList.valueOf(
                    "#FFFFFF".toColorInt()
                )
            binding.vFgMessage.show()
            if (fgMessage == null)
            {
                fgMessage = MessageFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(binding.vFgMessage.id,fgMessage!!).commit()
            }
        }
    }

    fun openProfile() {
        if (type != TYPE_PROFILE)
        {
            restViewNav()
            type = TYPE_PROFILE
            binding.icProfile.imageTintList =
                ColorStateList.valueOf(
                    "#FFFFFF".toColorInt()
                )
            binding.vFgProfile.show()
            if (fgProfile == null)
            {
                fgProfile = ProfileFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(binding.vFgProfile.id,fgProfile!!).commit()
            }
        }
    }

    fun openSearch() {
        if (type != TYPE_SEARCH)
        {
            restViewNav()
            type = TYPE_SEARCH
            binding.icSearch.imageTintList =
                ColorStateList.valueOf(
                    "#FFFFFF".toColorInt()
                )
            binding.vFgSearch.show()
            if (fgSearch == null)
            {
                fgSearch = SearchFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(binding.vFgSearch.id,fgSearch!!).commit()
            }
        }
    }

    fun openHome() {
        if (type != TYPE_HOME)
        {
            restViewNav()
            type = TYPE_HOME
            binding.icHome.imageTintList =
                ColorStateList.valueOf(
                    "#FFFFFF".toColorInt()
                )
            binding.vFgHome.show()
            if (fgHome == null)
            {
                fgHome = HomeFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(binding.vFgHome.id,fgHome!!).commit()
            }
        }
    }

    private fun initView() {
        restViewNav()
        type = TYPE_HOME
        binding.icHome.imageTintList =
            ColorStateList.valueOf(
                "#FFFFFF".toColorInt()
            )
        binding.vFgHome.show()
        fgHome = HomeFragment.newInstance()
        supportFragmentManager.beginTransaction().replace(binding.vFgHome.id, fgHome!!).commit()
    }

    private fun restViewNav() {
        binding.vFgHome.hide()
        binding.vFgSearch.hide()
        binding.vFgProfile.hide()
        binding.vFgMessage.hide()

        binding.icHome.imageTintList =
            ColorStateList.valueOf(
                "#767676".toColorInt()
            )
        binding.icSearch.imageTintList =
            ColorStateList.valueOf(
                "#767676".toColorInt()
            )
        binding.icProfile.imageTintList =
            ColorStateList.valueOf(
                "#767676".toColorInt()
            )
        binding.icMessage.imageTintList =
            ColorStateList.valueOf(
                "#767676".toColorInt()
            )
    }


}