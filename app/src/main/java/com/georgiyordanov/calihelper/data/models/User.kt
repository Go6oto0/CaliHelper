package com.georgiyordanov.calihelper.data.models

data class User(
    val role: String = "user",
    val uid: String = "",
    val email: String? = null,
    val profileSetup: Boolean = false,
    val userName: String? = null,
    val weight: Float? = null,
    val height: Float? = null,
    val age: Int? = null,
    val gender: String? = null,        // ← new
    val goal: String? = null,
    val progressLogIds: List<String> = emptyList(),
    val calorieLogIds: List<String> = emptyList(),
    val workoutPlanIds: List<String> = emptyList()
)

