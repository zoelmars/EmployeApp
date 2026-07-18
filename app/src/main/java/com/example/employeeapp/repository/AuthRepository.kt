package com.example.employeeapp.repository


import android.content.Context
import com.example.employeeapp.model.LoginRequest
import com.example.employeeapp.network.RetrofitClient
import com.example.employeeapp.util.Result
import com.example.employeeapp.util.SecureStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class AuthRepository(private val context: Context) {

    suspend fun login(email: String, password: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val apiService = RetrofitClient.getInstance(context)
                val response = apiService.login(LoginRequest(email, password))

                if (response.success && response.data != null) {
                    SecureStorage.saveToken(context, response.data.token)
                    SecureStorage.saveUserInfo(context, response.data.name, response.data.role)
                    SecureStorage.saveTokenExp(context, response.data.expiresAt)

                    RetrofitClient.resetInstance()

                    Result.Success(response.data.name)
                } else {
                    Result.Error(response.message.ifEmpty { "Login gagal" })
                }
            } catch (e: IOException) {
                Result.Error("Tidak ada koneksi internet. Periksa koneksi Anda.")
            } catch (e: HttpException) {
                val errorMessage = when (e.code()) {
                    401 -> "Email atau password salah"
                    404 -> "Endpoint API tidak ditemukan"
                    500 -> "Terjadi kesalahan pada server"
                    else -> "Server error: ${e.code()}"
                }
                Result.Error(errorMessage, e.code())
            } catch (e: Exception) {
                Result.Error(e.message ?: "Terjadi kesalahan tak terduga")
            }
        }
    }

    suspend fun logout(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val apiService = RetrofitClient.getInstance(context)
                try {
                    apiService.logout()
                } catch (_: Exception) {
                }

                SecureStorage.clearAll(context)
                RetrofitClient.resetInstance()

                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Logout gagal")
            }
        }
    }
}