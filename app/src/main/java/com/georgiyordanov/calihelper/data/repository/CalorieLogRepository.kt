package com.georgiyordanov.calihelper.data.repository

import android.util.Log
import com.georgiyordanov.calihelper.data.models.CalorieLog
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

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
    suspend fun updateLog(documentId: String, updates: Map<String, Any?>) {
        Log.d("CalorieLogRepo", "Attempting update on document: $documentId with updates: $updates")
        try {
            calorieLogsCollection.document(documentId).update(updates).await()
            Log.d("CalorieLogRepo", "Update successful on document: $documentId")
        } catch (e: Exception) {
            Log.e("CalorieLogRepo", "Error updating document: $documentId", e)
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
    suspend fun getOrCreateLogForDate(userId: String, localDate: LocalDate): Pair<CalorieLog, String> {
        val dateString = localDate.toString()  // e.g. "2025-04-13"

        // Query Firestore for a document with userId and date matching the string.
        val querySnapshot = calorieLogsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("date", dateString)
            .get()
            .await()

        return if (querySnapshot.isEmpty) {
            // Create a new CalorieLog if not found.
            val newLog = CalorieLog(
                userId = userId,
                caloriesBurned = 0,
                caloriesConsumed = 0,
                netCalories = 0,
                date = dateString,   // Stored as a String in Firestore.
                foodItems = emptyList()
            )
            val docRef = calorieLogsCollection.add(newLog).await()
            Pair(newLog, docRef.id)
        } else {
            // Use the first found document.
            val document = querySnapshot.documents.first()
            val log = document.toObject(CalorieLog::class.java)
                ?: throw Exception("Failed to deserialize CalorieLog")
            Pair(log, document.id)
        }
    }

    suspend fun addFoodItem(documentId: String, foodItem: Any) {
        // Log the attempt to update.
        android.util.Log.d("CalorieLogRepo", "Attempting atomic update on document: $documentId with FoodItem: $foodItem")
        try {
            calorieLogsCollection.document(documentId)
                .update("foodItems", FieldValue.arrayUnion(foodItem))
                .await()
            android.util.Log.d("CalorieLogRepo", "Atomic update successful for doc: $documentId")
        } catch (e: Exception) {
            android.util.Log.e("CalorieLogRepo", "Atomic update failed for doc: $documentId", e)
            throw e
        }
    }

}
