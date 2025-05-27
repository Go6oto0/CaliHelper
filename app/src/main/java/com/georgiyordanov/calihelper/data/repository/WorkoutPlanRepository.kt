// WorkoutPlanRepository.kt
package com.georgiyordanov.calihelper.data.repository

import android.util.Log
import com.georgiyordanov.calihelper.data.models.WorkoutPlan
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutPlanRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : IRepository<WorkoutPlan> {

    private val workoutPlansCollection: CollectionReference
        get() = firestore.collection("workoutPlans")

    override suspend fun create(entity: WorkoutPlan) {
        try {
            val docRef = if (entity.id.isBlank()) {
                workoutPlansCollection.document()
            } else {
                workoutPlansCollection.document(entity.id)
            }
            val updated = if (entity.id.isBlank()) {
                entity.copy(id = docRef.id)
            } else {
                entity
            }
            docRef.set(updated).await()
        } catch (e: Exception) {
            Log.e("WorkoutPlanRepo", "create failed", e)
            throw e
        }
    }

    override suspend fun read(workoutId: String): WorkoutPlan? {
        return try {
            val snap = workoutPlansCollection.document(workoutId).get().await()
            Log.d("WorkoutPlanRepo", "exists=$${snap.exists()}")
            snap.toObject(WorkoutPlan::class.java)
        } catch (e: Exception) {
            Log.e("WorkoutPlanRepo", "read failed", e)
            throw e
        }
    }

    override suspend fun readAll(): List<WorkoutPlan>? {
        return try {
            workoutPlansCollection.get().await().toObjects(WorkoutPlan::class.java)
        } catch (e: Exception) {
            Log.e("WorkoutPlanRepo", "readAll failed", e)
            throw e
        }
    }

    override suspend fun update(id: String, updates: Map<String, Any?>) {
        try {
            workoutPlansCollection.document(id).update(updates).await()
        } catch (e: Exception) {
            Log.e("WorkoutPlanRepo", "update failed", e)
            throw e
        }
    }

    override suspend fun delete(id: String) {
        try {
            workoutPlansCollection.document(id).delete().await()
        } catch (e: Exception) {
            Log.e("WorkoutPlanRepo", "delete failed", e)
            throw e
        }
    }
}
