package com.georgiyordanov.calihelper.data.repository

import android.util.Log
import com.georgiyordanov.calihelper.data.models.WorkoutPlan
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class WorkoutPlanRepository : IRepository<WorkoutPlan> {
    private val db = FirebaseFirestore.getInstance()
    private val workoutPlansCollection = db.collection("workoutPlans")

    override suspend fun create(entity: WorkoutPlan) {
        try {
            // If the entity does not already have an id, generate one:
            val documentRef = if (entity.id.isBlank()) {
                workoutPlansCollection.document()
            } else {
                workoutPlansCollection.document(entity.id)
            }
            // Copy the entity with the generated id if needed.
            val updatedEntity = if (entity.id.isBlank()) {
                entity.copy(id = documentRef.id)
            } else {
                entity
            }
            documentRef.set(updatedEntity).await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }


    // Inside WorkoutPlanRepository (pseudo-code)
    override suspend fun read(workoutId: String): WorkoutPlan? {
        val document = db.collection("workoutPlans").document(workoutId).get().await()
        Log.d("WorkoutPlanRepository", "Document for $workoutId exists: ${document.exists()}")
        return if (document.exists()) document.toObject(WorkoutPlan::class.java) else null
    }


    override suspend fun readAll(): List<WorkoutPlan>? {
        return try {
            val snapshot = workoutPlansCollection.get().await()
            snapshot.toObjects(WorkoutPlan::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun update(id: String, updates: Map<String, Any?>) {
        try {
            workoutPlansCollection.document(id).update(updates).await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun delete(id: String) {
        try {
            workoutPlansCollection.document(id).delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
