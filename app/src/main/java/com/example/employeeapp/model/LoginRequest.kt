package com.example.employeeapp.model


/**
 * Data class untuk body request login
 * Dikirim ke endpoint api/login.php
 */
data class LoginRequest(
    // Email user (harus terdaftar di tabel users)
    val email: String,

    // Password user (akan diverifikasi dengan bcrypt di server)
    val password: String
)