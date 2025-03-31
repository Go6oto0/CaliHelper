package com.georgiyordanov.calihelper.data.repository

import android.util.Log
import com.georgiyordanov.calihelper.data.models.ExerciseName
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ExerciseNameRepository : IRepository<ExerciseName> {
    private val db = FirebaseFirestore.getInstance()
    private val exerciseNamesCollection = db.collection("exerciseNames")

    override suspend fun create(entity: ExerciseName) {
        try {
            val documentRef = exerciseNamesCollection.document()
            documentRef.set(entity).await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun read(id: String): ExerciseName? {
        return try {
            val document = exerciseNamesCollection.document(id).get().await()
            if (document.exists()) {
                document.toObject(ExerciseName::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun readAll(): List<ExerciseName>? {
        return try {
            val snapshot = exerciseNamesCollection.get().await()
            val list = snapshot.documents.mapNotNull { doc ->
                doc.toObject(ExerciseName::class.java)?.apply { id = doc.id }
            }
            Log.d("ExerciseNameRepository", "Fetched exercises: $list")
            list
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }



    override suspend fun update(id: String, updates: Map<String, Any?>) {
        try {
            exerciseNamesCollection.document(id).update(updates).await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun delete(id: String) {
        try {
            exerciseNamesCollection.document(id).delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
