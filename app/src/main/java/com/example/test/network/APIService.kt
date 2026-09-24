package com.example.test.network

import com.example.test.model.login.Dto_login
import com.example.test.model.login.LoginRequest
import com.example.test.model.register.Dto_register
import com.example.test.model.register.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


interface APIService {




//    @GET("config_ai.php?os=2&type=1&country=VN&version=20241017")
//    suspend fun getDomainApi(
//        @Query("package") action: String,
//    ): Response<Dto_domain>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<Dto_login>


    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<Dto_register>



}