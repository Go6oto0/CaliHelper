package com.georgiyordanov.calihelper.data.repository

import com.georgiyordanov.calihelper.data.models.ExerciseName
import com.georgiyordanov.calihelper.data.models.WorkoutPlan
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class WorkoutSuggestorRepository {
    private val db = FirebaseFirestore.getInstance()

    /** Returns all exercise names for ID→name lookup. */
    suspend fun fetchExerciseNames(): List<ExerciseName> =
        db.collection("exerciseNames")
            .get().await()
            .toObjects(ExerciseName::class.java)

    /** Returns all suggested workouts of the given type. */
    suspend fun fetchSuggestions(type: String): List<WorkoutPlan> =
        db.collection("workoutPlanSuggestions")
            .whereEqualTo("type", type)
            .get().await()
            .toObjects(WorkoutPlan::class.java)
}
