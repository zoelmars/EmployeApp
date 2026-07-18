package com.example.employeeapp.network


import com.example.employeeapp.model.ApiResponse
import com.example.employeeapp.model.Employee
import com.example.employeeapp.model.LoginData
import com.example.employeeapp.model.LoginRequest
import retrofit2.http.*

interface ApiService {

    @POST("api/login.php")
    suspend fun login(
        @Body request: LoginRequest
    ): ApiResponse<LoginData>

    @POST("api/logout.php")
    suspend fun logout(): ApiResponse<Unit>

    @GET("api/employees.php")
    suspend fun getEmployees(
        @Query("search") search: String = "",
        @Query("department") department: String = ""
    ): ApiResponse<List<Employee>>

    @POST("api/employees.php")
    suspend fun createEmployee(
        @Body employee: Employee
    ): ApiResponse<Map<String, Int>>

    @GET("api/employee.php")
    suspend fun getEmployee(
        @Query("id") id: Int
    ): ApiResponse<Employee>

    @PUT("api/employee.php")
    suspend fun updateEmployee(
        @Query("id") id: Int,
        @Body employee: Employee
    ): ApiResponse<Unit>

    @DELETE("api/employee.php")
    suspend fun deleteEmployee(
        @Query("id") id: Int
    ): ApiResponse<Unit>
}