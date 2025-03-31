package com.georgiyordanov.calihelper.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://exercisedb-api.vercel.app/api/v1/"

    val api: ExerciseDbApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExerciseDbApiService::class.java)
    }
}
