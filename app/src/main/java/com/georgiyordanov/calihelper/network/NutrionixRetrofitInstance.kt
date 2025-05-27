package com.georgiyordanov.calihelper.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
object NutritionixRetrofitInstance {
    private const val BASE_URL = "https://trackapi.nutritionix.com/v2/"

    // Your keys (as given by Postman success)
    private const val API_KEY = "065b71dd3dc1b3f12d753c7623411315"
    private const val APP_ID = "4bf52ed2"

    private val client = OkHttpClient.Builder()
        // Note: now we pass the app ID first and then the API key:
        .addInterceptor(NutritionixInterceptor(API_KEY, APP_ID))
        .build()

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: NutritionixApiService by lazy {
        retrofit.create(NutritionixApiService::class.java)
    }
}
