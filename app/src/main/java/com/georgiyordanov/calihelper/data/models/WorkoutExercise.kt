package com.georgiyordanov.calihelper.data.models

data class WorkoutExercise(
    val workoutPlanId: Int,
    val exerciseId: Int,
    val repetitions: Int,
    val sets: Int
)
