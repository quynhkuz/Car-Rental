package com.example.test

import android.app.Application
import android.util.Log
import org.opencv.android.OpenCVLoader

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (OpenCVLoader.initLocal()) {
            Log.i("OpenCV", "OpenCV loaded successfully")
        } else {
            Log.e("OpenCV", "Failed to load OpenCV")
        }
    }

}