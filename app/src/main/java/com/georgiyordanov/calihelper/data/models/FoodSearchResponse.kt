package com.georgiyordanov.calihelper.data.models

import android.os.Parcelable
import java.io.Serializable

data class FoodSearchResponse(
    val common: List<CommonFood>?,
    val branded: List<BrandedFood>?
) : Serializable


data class CommonFood(
    val food_name: String,
    val serving_unit: String,
    val tag_name: String,
    val serving_qty: Double,
    val common_type: Any?, // Use a more specific type if possible
    val tag_id: String,
    val photo: FoodPhoto,
    val locale: String,
    // Nutrient fields (using Kotlin’s nullable types in case data is missing)
    val nf_calories: Double? = 0.0,
    val nf_total_carbohydrate: Double? = 0.0,
    val nf_total_fat: Double? = 0.0,
    val nf_protein: Double? = 0.0
) : Serializable

data class BrandedFood(
    val food_name: String,
    val serving_unit: String,
    val nix_brand_id: String,
    val brand_name_item_name: String,
    val serving_qty: Double,
    val nf_calories: Int,
    val photo: FoodPhoto,
    val brand_name: String,
    val region: Int,
    val brand_type: Int,
    val nix_item_id: String,
    val locale: String
) : Serializable

data class FoodPhoto(
    val thumb: String,
    val highres: String? = null,
    val is_user_uploaded: Boolean? = null
) : Serializable
