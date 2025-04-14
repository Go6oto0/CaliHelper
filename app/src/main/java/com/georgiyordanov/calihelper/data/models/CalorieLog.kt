package com.georgiyordanov.calihelper.data.models

import java.time.LocalDate

data class CalorieLog(
    val userId: String = "",
    val caloriesBurned: Int = 0,
    val caloriesConsumed: Int = 0,
    val netCalories: Int = 0,
    val date: String = "",
    val foodItems: List<FoodItem> = listOf()
)

