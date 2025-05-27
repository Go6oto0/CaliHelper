package com.georgiyordanov.calihelper.data.repository

import android.util.Log
import com.georgiyordanov.calihelper.data.models.CalorieLog
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalorieLogRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : IRepository<CalorieLog> {

    private val calorieLogsCollection: CollectionReference
        get() = firestore.collection("calorieLogs")

    /** Creates a new CalorieLog with auto-generated ID. */
    override suspend fun create(entity: CalorieLog) {
        try {
            val docRef = calorieLogsCollection.document()
            docRef.set(entity).await()
        } catch (e: Exception) {
            Log.e("CalorieLogRepo", "create() failed", e)
            throw e
        }
    }

    /** Reads a CalorieLog by its document ID, or returns null if not found. */
    override suspend fun read(id: String): CalorieLog? {
        return try {
            val snap = calorieLogsCollection.document(id).get().await()
            if (snap.exists()) snap.toObject(CalorieLog::class.java) else null
        } catch (e: Exception) {
            Log.e("CalorieLogRepo", "read() failed", e)
            throw e
        }
    }

    /** Reads all CalorieLog documents. */
    override suspend fun readAll(): List<CalorieLog>? {
        return try {
            calorieLogsCollection.get().await()
                .toObjects(CalorieLog::class.java)
        } catch (e: Exception) {
            Log.e("CalorieLogRepo", "readAll() failed", e)
            throw e
        }
    }

    /** Updates fields on a CalorieLog document. */
    override suspend fun update(id: String, updates: Map<String, Any?>) {
        try {
            calorieLogsCollection.document(id).update(updates).await()
        } catch (e: Exception) {
            Log.e("CalorieLogRepo", "update() failed", e)
            throw e
        }
    }

    /** Alias for update with additional logging. */
    suspend fun updateLog(documentId: String, updates: Map<String, Any?>) {
        Log.d("CalorieLogRepo", "updateLog(): id=$documentId, updates=$updates")
        try {
            calorieLogsCollection.document(documentId).update(updates).await()
            Log.d("CalorieLogRepo", "updateLog() succeeded for id=$documentId")
        } catch (e: Exception) {
            Log.e("CalorieLogRepo", "updateLog() failed for id=$documentId", e)
            throw e
        }
    }

    /** Deletes a CalorieLog by ID. */
    override suspend fun delete(id: String) {
        try {
            calorieLogsCollection.document(id).delete().await()
        } catch (e: Exception) {
            Log.e("CalorieLogRepo", "delete() failed", e)
            throw e
        }
    }

    /**
     * Fetches the log for the given user and date, creating it if missing.
     * Uses stable ID "${userId}_${dateString}".
     */
    suspend fun getOrCreateLogForDate(
        userId: String,
        localDate: LocalDate
    ): Pair<CalorieLog, String> {
        val dateString = localDate.toString() // e.g. "2025-04-13"
        val docId = "${userId}_$dateString"
        val docRef = calorieLogsCollection.document(docId)

        return try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                val existing = snap.toObject(CalorieLog::class.java)
                    ?: throw Exception("Failed to deserialize CalorieLog")
                existing to docId
            } else {
                val newLog = CalorieLog(
                    userId           = userId,
                    date             = dateString,
                    caloriesConsumed = 0,
                    caloriesBurned   = 0,
                    netCalories      = 0,
                    foodItems        = emptyList()
                )
                docRef.set(newLog).await()
                newLog to docId
            }
        } catch (e: Exception) {
            Log.e("CalorieLogRepo", "getOrCreateLogForDate() failed for id=$docId", e)
            throw e
        }
    }

    /**
     * Atomically appends a food item to the 'foodItems' array field.
     */
    suspend fun addFoodItem(documentId: String, foodItem: Any) {
        Log.d("CalorieLogRepo", "addFoodItem(): id=$documentId, item=$foodItem")
        try {
            calorieLogsCollection
                .document(documentId)
                .update("foodItems", FieldValue.arrayUnion(foodItem))
                .await()
            Log.d("CalorieLogRepo", "addFoodItem() succeeded for id=$documentId")
        } catch (e: Exception) {
            Log.e("CalorieLogRepo", "addFoodItem() failed for id=$documentId", e)
            throw e
        }
    }
}
