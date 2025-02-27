package com.georgiyordanov.calihelper.data.models

data class WorkoutPlan(
    val userId: Int,
    val name: String,
    val description: String,
    val exerciseIds: List<WorkoutExercise>
)
