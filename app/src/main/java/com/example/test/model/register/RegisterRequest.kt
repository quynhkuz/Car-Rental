package com.example.test.model.register

data class RegisterRequest(
    val email: Any,
    val fullName: String,
    val password: String,
    val phone: String
)