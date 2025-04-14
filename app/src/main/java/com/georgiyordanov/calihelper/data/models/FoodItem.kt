package com.georgiyordanov.calihelper.data.models

import java.io.Serializable
import java.util.UUID

data class FoodItem(
    val id: String = UUID.randomUUID().toString(), // Always creates a unique id.
    val name: String = "",
    val calories: Int = 0,
    val servingSize: String = "",
    val protein: Float = 0.0f,
    val fat: Float = 0.0f,
    val carbohydrates: Float = 0.0f
) : Serializable
