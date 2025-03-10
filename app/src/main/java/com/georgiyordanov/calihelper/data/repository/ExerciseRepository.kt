package com.georgiyordanov.calihelper.data.repository

import com.georgiyordanov.calihelper.data.models.Exercise
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ExerciseRepository : IRepository<Exercise> {
    private val db = FirebaseFirestore.getInstance()
    private val exercisesCollection = db.collection("exercises")

    override suspend fun create(entity: Exercise) {
        try {
            val documentRef = exercisesCollection.document()
            documentRef.set(entity).await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun read(id: String): Exercise? {
        return try {
            val document = exercisesCollection.document(id).get().await()
            if (document.exists()) {
                document.toObject(Exercise::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun readAll(): List<Exercise>? {
        return try {
            val snapshot = exercisesCollection.get().await()
            snapshot.toObjects(Exercise::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun update(id: String, updates: Map<String, Any?>) {
        try {
            exercisesCollection.document(id).update(updates).await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun delete(id: String) {
        try {
            exercisesCollection.document(id).delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
