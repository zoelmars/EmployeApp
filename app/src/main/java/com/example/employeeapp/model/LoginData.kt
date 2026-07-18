package com.example.employeeapp.model


import com.google.gson.annotations.SerializedName

data class LoginData(
    val token: String,
    val name: String,
    val role: String,
    @SerializedName("expires_at")
    val expiresAt: String
)