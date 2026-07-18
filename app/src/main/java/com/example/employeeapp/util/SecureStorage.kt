package com.example.employeeapp.util


import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Object untuk menyimpan data sensitif secara terenkripsi
 * Menggunakan EncryptedSharedPreferences + Android Keystore
 * Data yang disimpan: API Key, Token, Nama User, Role, Expired Time
 */
object SecureStorage {

    // Nama file SharedPreferences (akan tersimpan terenkripsi)
    private const val PREFS_NAME = "secure_employee_prefs"

    // Key untuk masing-masing data yang disimpan
    private const val KEY_API_KEY = "api_key"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_USERNAME = "username"
    private const val KEY_ROLE = "user_role"
    private const val KEY_TOKEN_EXP = "token_exp"

    // API Key default (sebenarnya ini hardcode, tapi disimpan terenkripsi)
    // Harus sama dengan yang ada di config/db.php
    private const val DEFAULT_API_KEY = "EmpApp_SecureKey_2024_XYZ789"

    /**
     * Membuat atau mendapatkan instance EncryptedSharedPreferences
     * @param ctx Context aplikasi
     * @return SharedPreferences yang terenkripsi
     */
    private fun getEncryptedPrefs(ctx: Context): SharedPreferences {
        // Membuat MasterKey yang disimpan di Android Keystore
        // AES256_GCM adalah algoritma enkripsi standar
        val masterKey = MasterKey.Builder(ctx)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        // Membuat EncryptedSharedPreferences dengan skema enkripsi:
        // - Key enkripsi: AES256_SIV
        // - Value enkripsi: AES256_GCM
        return EncryptedSharedPreferences.create(
            ctx,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Mendapatkan API Key yang tersimpan (atau menyimpan default jika belum ada)
     * @param ctx Context aplikasi
     * @return API Key dalam bentuk String
     */
    fun getApiKey(ctx: Context): String {
        val prefs = getEncryptedPrefs(ctx)
        val storedKey = prefs.getString(KEY_API_KEY, null)

        // Jika belum ada API Key, simpan default key
        if (storedKey.isNullOrEmpty()) {
            prefs.edit().putString(KEY_API_KEY, DEFAULT_API_KEY).apply()
            return DEFAULT_API_KEY
        }
        return storedKey
    }

    /**
     * Menyimpan Bearer Token setelah login sukses
     * @param ctx Context aplikasi
     * @param token Token yang akan disimpan
     */
    fun saveToken(ctx: Context, token: String) {
        getEncryptedPrefs(ctx).edit().putString(KEY_TOKEN, token).apply()
    }

    /**
     * Mendapatkan Bearer Token yang tersimpan
     * @param ctx Context aplikasi
     * @return Token dalam bentuk String (kosong jika belum login)
     */
    fun getToken(ctx: Context): String {
        return getEncryptedPrefs(ctx).getString(KEY_TOKEN, "") ?: ""
    }

    /**
     * Menyimpan informasi user (nama dan role)
     * @param ctx Context aplikasi
     * @param name Nama user
     * @param role Role user (admin/hr/viewer)
     */
    fun saveUserInfo(ctx: Context, name: String, role: String) {
        getEncryptedPrefs(ctx).edit().apply {
            putString(KEY_USERNAME, name)
            putString(KEY_ROLE, role)
            apply()
        }
    }

    /**
     * Mendapatkan nama user yang login
     * @param ctx Context aplikasi
     * @return Nama user (default "User" jika belum login)
     */
    fun getUsername(ctx: Context): String {
        return getEncryptedPrefs(ctx).getString(KEY_USERNAME, "User") ?: "User"
    }

    /**
     * Menyimpan waktu expired token
     * @param ctx Context aplikasi
     * @param exp Waktu expired dalam format "YYYY-MM-DD HH:MM:SS"
     */
    fun saveTokenExp(ctx: Context, exp: String) {
        getEncryptedPrefs(ctx).edit().putString(KEY_TOKEN_EXP, exp).apply()
    }

    /**
     * Mengecek apakah user sudah login (token ada)
     * @param ctx Context aplikasi
     * @return true jika token tersedia dan belum expired
     */
    fun isLoggedIn(ctx: Context): Boolean {
        val token = getToken(ctx)
        return token.isNotEmpty() && !isTokenExpired(ctx)
    }

    /**
     * Mengecek apakah token sudah expired
     * @param ctx Context aplikasi
     * @return true jika token sudah expired atau tidak ada informasi expired
     */
    fun isTokenExpired(ctx: Context): Boolean {
        val expStr = getEncryptedPrefs(ctx).getString(KEY_TOKEN_EXP, null) ?: return true

        return try {
            // Parse string expired ke LocalDateTime
            // Ganti spasi dengan 'T' agar sesuai format ISO
            val expTime = LocalDateTime.parse(expStr.replace(" ", "T"))
            val now = LocalDateTime.now()
            now.isAfter(expTime) // true jika sekarang > waktu expired
        } catch (e: Exception) {
            true // Jika parsing gagal, anggap expired
        }
    }

    /**
     * Menghapus semua data yang tersimpan (digunakan saat logout)
     * @param ctx Context aplikasi
     */
    fun clearAll(ctx: Context) {
        getEncryptedPrefs(ctx).edit().clear().apply()
    }
}