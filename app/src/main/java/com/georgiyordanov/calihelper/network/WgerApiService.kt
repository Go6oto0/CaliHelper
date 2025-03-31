/*package com.georgiyordanov.calihelper.network

import com.georgiyordanov.calihelper.data.models.ExerciseImageResponse
import com.georgiyordanov.calihelper.data.models.ExerciseResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WgerApiService {
    @GET("exercise/")
    suspend fun getExercises(
        @Query("id") id: Int? = null,
        @Query("language") language: Int = 2,  // e.g., 2 for English
        @Query("limit") limit: Int = 1000       // Increase the limit to fetch more exercises
    ): ExerciseResponse

    @GET("exerciseimage/")
    suspend fun getExerciseImages(
        @Query("exercise") exerciseId: Int,
        @Query("is_main") isMain: Boolean = true
    ): ExerciseImageResponse
}*/