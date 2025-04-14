package com.georgiyordanov.calihelper.data.repository

import com.georgiyordanov.calihelper.data.models.Nutrients
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class NutrientsRepository : IRepository<Nutrients> {
    private val db = FirebaseFirestore.getInstance()
    private val nutrientsCollection = db.collection("nutrients")

    override suspend fun create(entity: Nutrients) {
        try {
            val documentRef = nutrientsCollection.document()
            documentRef.set(entity).await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun read(id: String): Nutrients? {
        return try {
            val document = nutrientsCollection.document(id).get().await()
            if (document.exists()) {
                document.toObject(Nutrients::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun readAll(): List<Nutrients>? {
        return try {
            val snapshot = nutrientsCollection.get().await()
            snapshot.toObjects(Nutrients::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun update(id: String, updates: Map<String, Any?>) {
        try {
            nutrientsCollection.document(id).update(updates).await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun delete(id: String) {
        try {
            nutrientsCollection.document(id).delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
