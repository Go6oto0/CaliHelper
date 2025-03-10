package com.georgiyordanov.calihelper.data.repository

import com.georgiyordanov.calihelper.data.models.CalorieLog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CalorieLogRepository : IRepository<CalorieLog> {
    private val db = FirebaseFirestore.getInstance()
    private val calorieLogsCollection = db.collection("calorieLogs")

    override suspend fun create(entity: CalorieLog) {
        try {
            val documentRef = calorieLogsCollection.document()
            documentRef.set(entity).await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun read(id: String): CalorieLog? {
        return try {
            val document = calorieLogsCollection.document(id).get().await()
            if (document.exists()) {
                document.toObject(CalorieLog::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun readAll(): List<CalorieLog>? {
        return try {
            val snapshot = calorieLogsCollection.get().await()
            snapshot.toObjects(CalorieLog::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun update(id: String, updates: Map<String, Any?>) {
        try {
            calorieLogsCollection.document(id).update(updates).await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun delete(id: String) {
        try {
            calorieLogsCollection.document(id).delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
