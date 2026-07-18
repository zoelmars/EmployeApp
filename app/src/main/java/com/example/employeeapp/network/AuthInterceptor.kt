package com.example.employeeapp.network

import android.content.Context
import com.example.employeeapp.util.SecureStorage
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        val apiKey = SecureStorage.getApiKey(context)
        requestBuilder.addHeader("X-API-KEY", apiKey)

        val token = SecureStorage.getToken(context)
        if (token.isNotEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        requestBuilder.addHeader("Content-Type", "application/json")

        val modifiedRequest = requestBuilder.build()
        val response = chain.proceed(modifiedRequest)

        if (response.code == 401) {
            SecureStorage.clearAll(context)
        }

        return response
    }
}