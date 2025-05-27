package com.georgiyordanov.calihelper.data.repository

import android.util.Log
import com.georgiyordanov.calihelper.data.models.FoodItem
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodItemRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : IRepository<FoodItem> {

    private val foodItemsCollection: CollectionReference
        get() = firestore.collection("foodItems")

    /** Creates a new FoodItem document with an auto‐generated ID. */
    override suspend fun create(entity: FoodItem) {
        try {
            val docRef = foodItemsCollection.document()
            docRef.set(entity).await()
        } catch (e: Exception) {
            Log.e("FoodItemRepository", "create() failed", e)
            throw e
        }
    }

    /** Reads a FoodItem by document ID, or returns null if not found. */
    override suspend fun read(id: String): FoodItem? {
        return try {
            val snap = foodItemsCollection.document(id).get().await()
            if (snap.exists()) snap.toObject(FoodItem::class.java) else null
        } catch (e: Exception) {
            Log.e("FoodItemRepository", "read() failed", e)
            throw e
        }
    }

    /** Reads all FoodItem documents in the collection. */
    override suspend fun readAll(): List<FoodItem>? {
        return try {
            foodItemsCollection.get().await()
                .toObjects(FoodItem::class.java)
        } catch (e: Exception) {
            Log.e("FoodItemRepository", "readAll() failed", e)
            throw e
        }
    }

    /** Applies a partial update to a FoodItem document. */
    override suspend fun update(id: String, updates: Map<String, Any?>) {
        try {
            foodItemsCollection.document(id).update(updates).await()
        } catch (e: Exception) {
            Log.e("FoodItemRepository", "update() failed", e)
            throw e
        }
    }

    /** Deletes a FoodItem document by ID. */
    override suspend fun delete(id: String) {
        try {
            foodItemsCollection.document(id).delete().await()
        } catch (e: Exception) {
            Log.e("FoodItemRepository", "delete() failed", e)
            throw e
        }
    }
}
