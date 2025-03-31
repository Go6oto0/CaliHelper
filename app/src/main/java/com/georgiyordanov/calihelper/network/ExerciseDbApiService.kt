package com.georgiyordanov.calihelper.network

import com.georgiyordanov.calihelper.data.models.Exercise
import com.georgiyordanov.calihelper.data.models.ExercisesApiResponse
import com.georgiyordanov.calihelper.data.models.SingleExerciseResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ExerciseDbApiService {

    // Method to fetch all exercises.
    @GET("exercises")
    suspend fun getAllExercises(
        @Query("search") search: String? = null,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 10
    ): ExercisesApiResponse

    // Method to fetch a single exercise by its id.
    @GET("exercises/{id}")
    suspend fun getExerciseById(
        @Path("id") id: String
    ): SingleExerciseResponse
}
