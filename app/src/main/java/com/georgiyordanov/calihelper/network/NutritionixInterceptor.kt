package com.georgiyordanov.calihelper.network

import okhttp3.Interceptor
import okhttp3.Response

class NutritionixInterceptor(private val apiKey: String, private val appId: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("x-app-id", appId)
            .addHeader("x-app-key", apiKey)
            .build()
        return chain.proceed(request)
    }
}