package com.georgiyordanov.calihelper.data.models

data class User(
    val role: String = "user",
    val uid: String = "", // Default value to help with no-arg constructor requirements.
    val email: String? = null,
    val profileSetup: Boolean = false,
    val userName: String? = null,
    val weight: Float? = null,
    val height: Float? = null,
    val age: Int? = null,  // Changed from Short? to Int? for better serialization.
    val goal: String? = null,
    val progressLogIds: List<String> = emptyList(),
    val calorieLogIds: List<String> = emptyList(),
    val workoutPlanIds: List<String> = emptyList()
)
