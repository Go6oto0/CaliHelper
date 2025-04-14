package com.georgiyordanov.calihelper.data.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class NutrientRequest(
    val query: String
) : Serializable

data class NutrientResponse(
    val foods: List<FoodDetail>
) : Serializable

data class FoodDetail(
    @SerializedName("food_name")
    val food_name: String,
    @SerializedName("serving_qty")
    val serving_qty: Double,
    @SerializedName("serving_unit")
    val serving_unit: String,
    @SerializedName("serving_weight_grams")
    val serving_weight_grams: Double,
    @SerializedName("nf_calories")
    val nf_calories: Double,
    @SerializedName("nf_total_carbohydrate")
    val nf_total_carbohydrate: Double,
    @SerializedName("nf_total_fat")
    val nf_total_fat: Double,
    @SerializedName("nf_protein")
    val nf_protein: Double
) : Serializable
