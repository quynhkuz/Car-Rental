package com.example.test.network

import com.example.test.model.LoginRequest
import com.example.test.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface APIService {




//    @GET("config_ai.php?os=2&type=1&country=VN&version=20241017")
//    suspend fun getDomainApi(
//        @Query("package") action: String,
//    ): Response<Dto_domain>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>


}