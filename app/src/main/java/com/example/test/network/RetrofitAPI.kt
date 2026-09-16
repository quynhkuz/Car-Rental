package com.example.test.network

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
//import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object RetrofitAPI {


    private val uploadLogger = Interceptor { chain ->
        val request = chain.request()

        if (request.body is MultipartBody) {
            val multipart = request.body as MultipartBody
            Log.d("UPLOAD", "==== Multipart Request Info ====")
            Log.d("UPLOAD", "URL: ${request.url}")
            multipart.parts.forEachIndexed { index, part ->
                val headers = part.headers
                val body = part.body
                val size = try {
                    body.contentLength()
                } catch (e: IOException) {
                    -1L
                }
                Log.d(
                    "UPLOAD",
                    "Part[$index]: headers=${headers}, size=${size} bytes"
                )
            }
            Log.d("UPLOAD", "===============================")
        }
        chain.proceed(request)
    }

    private val httpLogger = HttpLoggingInterceptor { message ->
        Log.d("HTTP", message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }



    var apiService : APIService = Retrofit.Builder()
        .client(getRetrofitClient())
        .baseUrl("https://sascorpvn.com/")
        .addConverterFactory(GsonConverterFactory.create(getGson()))
        .build()
        .create(APIService::class.java)




    fun getGson() : Gson
    {
        return GsonBuilder()
//            .registerTypeAdapter(Date::class.java, UnitEpochDateTypeAdapter())
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .setLenient()
            .create()
    }

    private fun getRetrofitClient(): OkHttpClient {
        return  OkHttpClient.Builder()
            .addInterceptor(uploadLogger) // custom log ảnh
            .addInterceptor(httpLogger)   // log body text / JSON
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }


}