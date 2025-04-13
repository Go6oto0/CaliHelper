package com.georgiyordanov.calihelper.data.repository

import android.util.Log
import com.georgiyordanov.calihelper.data.models.WorkoutExercise
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class WorkoutExerciseRepository : IRepository<WorkoutExercise> {
    private val db = FirebaseFirestore.getInstance()
    private val workoutExercisesCollection = db.collection("workoutExercises")

    override suspend fun create(entity: WorkoutExercise) {
        try {
            // Use the entity's id if it's non-blank; otherwise, generate a new one.
            val docRef = if (entity.id.isBlank()) {
                workoutExercisesCollection.document()
            } else {
                workoutExercisesCollection.document(entity.id)
            }
            val updatedEntity = if (entity.id.isBlank()) {
                entity.copy(id = docRef.id)
            } else {
                entity
            }
            docRef.set(updatedEntity).await()
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
        try {
            Log.d("WorkoutExerciseRepository", "Starting readAll()")
            val snapshot = workoutExercisesCollection.get().await()
            val list = snapshot.toObjects(WorkoutExercise::class.java)
            Log.d("WorkoutExerciseRepository", "Finished readAll(), count = ${list.size}")
            return list
        } catch (e: Exception) {
            Log.e("WorkoutExerciseRepository", "Error in readAll()", e)
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
