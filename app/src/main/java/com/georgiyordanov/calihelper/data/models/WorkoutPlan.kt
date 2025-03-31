package com.georgiyordanov.calihelper.data.models

data class WorkoutPlan(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    val exercises: List<WorkoutExercise> = emptyList()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "name" to name,
            "description" to description,
            "exercises" to exercises
        )
    }
}


