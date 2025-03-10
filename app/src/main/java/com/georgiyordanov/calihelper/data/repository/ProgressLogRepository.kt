package com.georgiyordanov.calihelper.data.repository

import com.georgiyordanov.calihelper.data.models.ProgressLog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProgressLogRepository : IRepository<ProgressLog> {
    private val db = FirebaseFirestore.getInstance()
    private val progressLogsCollection = db.collection("progressLogs")

    override suspend fun create(entity: ProgressLog) {
        try {
            val documentRef = progressLogsCollection.document()
            documentRef.set(entity).await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun read(id: String): ProgressLog? {
        return try {
            val document = progressLogsCollection.document(id).get().await()
            if (document.exists()) {
                document.toObject(ProgressLog::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun readAll(): List<ProgressLog>? {
        return try {
            val snapshot = progressLogsCollection.get().await()
            snapshot.toObjects(ProgressLog::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun update(id: String, updates: Map<String, Any?>) {
        try {
            progressLogsCollection.document(id).update(updates).await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun delete(id: String) {
        try {
            progressLogsCollection.document(id).delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
