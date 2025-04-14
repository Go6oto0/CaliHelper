package com.georgiyordanov.calihelper.network

import com.georgiyordanov.calihelper.data.models.FoodSearchResponse
import com.georgiyordanov.calihelper.data.models.NutrientRequest
import com.georgiyordanov.calihelper.data.models.NutrientResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface NutritionixApiService {
    @GET("search/instant")
    suspend fun searchFood(
        @Query("query") query: String
    ): Response<FoodSearchResponse>
    @POST("natural/nutrients")
    suspend fun getNutrientInfo(
        @Body request: NutrientRequest
    ): Response<NutrientResponse>
}
