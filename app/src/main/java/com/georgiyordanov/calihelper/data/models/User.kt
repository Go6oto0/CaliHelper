package com.georgiyordanov.calihelper.data.models

data class User(
    val role: String = "user",
    val uid: String,
    val userName: String? = null,
    val weight: Float? = null,
    val height: Float? = null,
    val goal: String? = null,
    val progressLogIds: List<String> = emptyList(),
    val calorieLogIds: List<String> = emptyList(),
    val workoutPlanIds: List<String> = emptyList()
)

