package com.georgiyordanov.calihelper.data.models

data class CalorieLog(
    val userId: Int,
    val caloriesBurned: Int,
    val caloriesConsumed: Int,
    val netCalories: Int,
    val date: String
)
