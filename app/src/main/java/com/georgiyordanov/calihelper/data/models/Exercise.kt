package com.georgiyordanov.calihelper.data.models

import java.io.Serializable

data class Exercise(
    val exerciseId: String,
    val name: String,
    val gifUrl: String,
    val instructions: List<String>,
    val targetMuscles: List<String>,
    val bodyParts: List<String>,
    val equipments: List<String>,
    val secondaryMuscles: List<String>
) : Serializable

data class ExercisesApiResponse(
    val success: Boolean,
    val data: ExercisesData
)

data class ExercisesData(
    val previousPage: String?,
    val nextPage: String?,
    val totalPages: Int,
    val totalExercises: Int,
    val currentPage: Int,
    val exercises: List<Exercise>
)
data class SingleExerciseResponse(
    val success: Boolean,
    val data: Exercise
)