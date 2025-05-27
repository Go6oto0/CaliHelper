package com.georgiyordanov.calihelper.data.repository

import android.util.Log
import com.georgiyordanov.calihelper.data.models.WorkoutExercise
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutExerciseRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : IRepository<WorkoutExercise> {

    private val workoutExercisesCollection: CollectionReference
        get() = firestore.collection("workoutExercises")

    override suspend fun create(entity: WorkoutExercise) {
        try {
            val docRef = if (entity.id.isBlank()) {
                workoutExercisesCollection.document()
            } else {
                workoutExercisesCollection.document(entity.id)
            }
            val updated = if (entity.id.isBlank()) {
                entity.copy(id = docRef.id)
            } else {
                entity
            }
            docRef.set(updated).await()
        } catch (e: Exception) {
            Log.e("WorkoutExerciseRepo", "create failed", e)
            throw e
        }
    }

    override suspend fun read(id: String): WorkoutExercise? {
        return try {
            val snap = workoutExercisesCollection.document(id).get().await()
            if (snap.exists()) snap.toObject(WorkoutExercise::class.java) else null
        } catch (e: Exception) {
            Log.e("WorkoutExerciseRepo", "read failed", e)
            throw e
        }
    }

    override suspend fun readAll(): List<WorkoutExercise>? {
        return try {
            Log.d("WorkoutExerciseRepo", "readAll() starting")
            val list = workoutExercisesCollection.get().await()
                .toObjects(WorkoutExercise::class.java)
            Log.d("WorkoutExerciseRepo", "readAll() fetched ${list.size} items")
            list
        } catch (e: Exception) {
            Log.e("WorkoutExerciseRepo", "readAll failed", e)
            throw e
        }
    }

    override suspend fun update(id: String, updates: Map<String, Any?>) {
        try {
            workoutExercisesCollection.document(id).update(updates).await()
        } catch (e: Exception) {
            Log.e("WorkoutExerciseRepo", "update failed", e)
            throw e
        }
    }

    override suspend fun delete(id: String) {
        try {
            workoutExercisesCollection.document(id).delete().await()
        } catch (e: Exception) {
            Log.e("WorkoutExerciseRepo", "delete failed", e)
            throw e
        }
    }
}
