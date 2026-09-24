package com.example.test.viewmodel

import android.util.Log
import com.example.test.model.login.Dto_login
import com.example.test.model.login.LoginRequest
import com.example.test.network.RetrofitAPI
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class CarRepository @Inject constructor() {

    // ---- Helper 1: bọc MỌI api call vào Result, tự log lỗi ----
    private suspend fun <T> safeApiCall(
        tag: String = "API",
        apiCall: suspend () -> Response<T>,
    ): Result<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Response rỗng"))
            } else {
                Log.e(tag, "Lỗi HTTP: ${response.code()}")
                Result.failure(Exception("Lỗi HTTP: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(tag, "Lỗi: ${e.message}")
            Result.failure(e)
        }
    }

    // ---- Helper 2: tạo multipart part từ đường dẫn file ảnh ----
    private fun String.toImagePart(formName: String): MultipartBody.Part {
        val file = File(this)
        val body = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(formName, file.name, body)
    }

    fun String.toTextBody() =
        toRequestBody("text/plain".toMediaType())

    private suspend fun <T> handleApi(
        cacheData: T?,
        apiCall: suspend () -> Response<T>,
        saveCache: (T) -> Unit
    ): Result<T> {
        cacheData?.let {
            return Result.success(it)
        }
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                response.body()?.let { data ->
                    saveCache(data)
                    Result.success(data)
                } ?: Result.failure(Exception("Response rỗng"))
            } else {
                Result.failure(Exception("Lỗi HTTP: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun login(request: LoginRequest) : Result<Dto_login> {
        return safeApiCall {
            RetrofitAPI.apiService.login(request)
        }
    }



}