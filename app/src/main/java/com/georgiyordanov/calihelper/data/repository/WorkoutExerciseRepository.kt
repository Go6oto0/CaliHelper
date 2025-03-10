package com.georgiyordanov.calihelper.data.repository

import com.georgiyordanov.calihelper.data.models.WorkoutExercise
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class WorkoutExerciseRepository : IRepository<WorkoutExercise> {
    private val db = FirebaseFirestore.getInstance()
    private val workoutExercisesCollection = db.collection("workoutExercises")

    override suspend fun create(entity: WorkoutExercise) {
        try {
            val documentRef = workoutExercisesCollection.document()
            documentRef.set(entity).await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun read(id: String): WorkoutExercise? {
        return try {
            val document = workoutExercisesCollection.document(id).get().await()
            if (document.exists()) {
                document.toObject(WorkoutExercise::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun readAll(): List<WorkoutExercise>? {
        return try {
            val snapshot = workoutExercisesCollection.get().await()
            snapshot.toObjects(WorkoutExercise::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun update(id: String, updates: Map<String, Any?>) {
        try {
            workoutExercisesCollection.document(id).update(updates).await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun delete(id: String) {
        try {
            workoutExercisesCollection.document(id).delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
