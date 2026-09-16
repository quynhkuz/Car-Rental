package com.example.test.core

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit


class MySharedPreferences(context: Context) {

    private var prefs: SharedPreferences =
        context.getSharedPreferences("${context.packageName}", Context.MODE_PRIVATE)

    companion object {
        const val FIRST_RUN = "first_run"
        const val LANGUAGE = "language"

    }



    fun attachFistRun(b: Boolean) {
        prefs.edit() {
            putBoolean(FIRST_RUN, b)
        }
    }

    fun fetchFistRun(): Boolean {
        return prefs.getBoolean(FIRST_RUN, true)
    }


}