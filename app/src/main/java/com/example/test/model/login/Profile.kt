package com.example.test.model.login

data class Profile(
    val documentStatus: String,
    val email: String,
    val fullName: String,
    val id: Int,
    val isOwner: Boolean,
    val phone: String,
    val status: String
)