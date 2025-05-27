package com.georgiyordanov.calihelper.data.repository

import android.util.Log
import com.georgiyordanov.calihelper.data.models.ExerciseName
import com.georgiyordanov.calihelper.data.models.WorkoutPlan
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutSuggestorRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private val exerciseNamesCollection: CollectionReference
        get() = firestore.collection("exerciseNames")

    private val suggestionsCollection: CollectionReference
        get() = firestore.collection("workoutPlanSuggestions")

    /** Returns all exercise names for ID→name lookup. */
    suspend fun fetchExerciseNames(): List<ExerciseName> = try {
        exerciseNamesCollection
            .get()
            .await()
            .toObjects(ExerciseName::class.java)
    } catch (e: Exception) {
        Log.e("WorkoutSuggestorRepo", "fetchExerciseNames failed", e)
        throw e
    }

    /** Returns all suggested workouts of the given type. */
    suspend fun fetchSuggestions(type: String): List<WorkoutPlan> = try {
        suggestionsCollection
            .whereEqualTo("type", type)
            .get()
            .await()
            .toObjects(WorkoutPlan::class.java)
    } catch (e: Exception) {
        Log.e("WorkoutSuggestorRepo", "fetchSuggestions failed", e)
        throw e
    }
}
