package com.georgiyordanov.calihelper.data.repository

import com.georgiyordanov.calihelper.data.models.FoodItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FoodItemRepository : IRepository<FoodItem> {
    private val db = FirebaseFirestore.getInstance()
    private val foodItemsCollection = db.collection("foodItems")

    override suspend fun create(entity: FoodItem) {
        try {
            // Let Firestore generate an ID automatically.
            val documentRef = foodItemsCollection.document()
            documentRef.set(entity).await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun read(id: String): FoodItem? {
        return try {
            val document = foodItemsCollection.document(id).get().await()
            if (document.exists()) {
                document.toObject(FoodItem::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun readAll(): List<FoodItem>? {
        return try {
            val snapshot = foodItemsCollection.get().await()
            snapshot.toObjects(FoodItem::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun update(id: String, updates: Map<String, Any?>) {
        try {
            foodItemsCollection.document(id).update(updates).await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun delete(id: String) {
        try {
            foodItemsCollection.document(id).delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
