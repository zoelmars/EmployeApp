package com.example.employeeapp.repository


import android.content.Context
import com.example.employeeapp.model.ApiResponse
import com.example.employeeapp.model.Employee
import com.example.employeeapp.network.RetrofitClient
import com.example.employeeapp.util.Result
import com.example.employeeapp.util.SecureStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class EmployeeRepository(private val context: Context) {

    private suspend fun <T> safeApiCall(block: suspend () -> ApiResponse<T>): Result<T> {
        return try {
            if (SecureStorage.isTokenExpired(context)) {
                return Result.Error("Sesi telah berakhir, silakan login ulang", 401)
            }

            val response = block()

            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(response.message.ifEmpty { "Terjadi kesalahan" })
            }
        } catch (e: IOException) {
            Result.Error("Tidak ada koneksi internet. Periksa koneksi Anda.")
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                401 -> "Sesi habis, silakan login ulang"
                404 -> "Data tidak ditemukan"
                409 -> "Email sudah terdaftar"
                else -> "HTTP Error: ${e.code()}"
            }
            Result.Error(errorMessage, e.code())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Terjadi kesalahan")
        }
    }

    suspend fun getAllEmployees(search: String = "", department: String = ""): Result<List<Employee>> {
        return safeApiCall {
            val apiService = RetrofitClient.getInstance(context)
            apiService.getEmployees(search, department)
        }
    }

    suspend fun getEmployeeById(id: Int): Result<Employee> {
        return safeApiCall {
            val apiService = RetrofitClient.getInstance(context)
            apiService.getEmployee(id)
        }
    }

    suspend fun createEmployee(employee: Employee): Result<Map<String, Int>> {
        return safeApiCall {
            val apiService = RetrofitClient.getInstance(context)
            apiService.createEmployee(employee)
        }
    }

    suspend fun updateEmployee(id: Int, employee: Employee): Result<Unit> {
        return safeApiCall {
            val apiService = RetrofitClient.getInstance(context)
            apiService.updateEmployee(id, employee)
        }
    }

    suspend fun deleteEmployee(id: Int): Result<Unit> {
        return safeApiCall {
            val apiService = RetrofitClient.getInstance(context)
            apiService.deleteEmployee(id)
        }
    }
}