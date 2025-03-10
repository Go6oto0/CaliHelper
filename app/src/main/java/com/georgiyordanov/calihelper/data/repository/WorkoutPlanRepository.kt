package com.georgiyordanov.calihelper.data.repository

import com.georgiyordanov.calihelper.data.models.WorkoutPlan
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class WorkoutPlanRepository : IRepository<WorkoutPlan> {
    private val db = FirebaseFirestore.getInstance()
    private val workoutPlansCollection = db.collection("workoutPlans")

    override suspend fun create(entity: WorkoutPlan) {
        try {
            val documentRef = workoutPlansCollection.document()
            documentRef.set(entity).await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun read(id: String): WorkoutPlan? {
        return try {
            val document = workoutPlansCollection.document(id).get().await()
            if (document.exists()) {
                document.toObject(WorkoutPlan::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
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
