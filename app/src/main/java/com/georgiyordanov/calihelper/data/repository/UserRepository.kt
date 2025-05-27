package com.georgiyordanov.calihelper.data.repository

import android.util.Log
import com.georgiyordanov.calihelper.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : IRepository<User> {

    private val usersCollection: CollectionReference
        get() = firestore.collection("users")

    /**
     * Creates or overwrites a user document with the given entity.
     */
    override suspend fun create(entity: User) {
        try {
            Log.d("UserRepository", "Creating user: $entity")
            usersCollection
                .document(entity.uid)
                .set(entity)
                .await()
        } catch (e: Exception) {
            Log.e("UserRepository", "create() failed", e)
            throw e
        }
    }

    /**
     * Reads a user by UID. Returns null if not found.
     */
    override suspend fun read(id: String): User? {
        return try {
            val snap = usersCollection.document(id).get().await()
            if (snap.exists()) {
                snap.toObject(User::class.java)
            } else {
                Log.w("UserRepository", "read(): user $id not found")
                null
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "read() failed", e)
            throw e
        }
    }

    /**
     * Reads all users. May return empty list.
     */
    override suspend fun readAll(): List<User>? {
        return try {
            usersCollection.get().await()
                .toObjects(User::class.java)
        } catch (e: Exception) {
            Log.e("UserRepository", "readAll() failed", e)
            throw e
        }
    }

    /**
     * Merges the provided fields into the existing user document.
     */
    override suspend fun update(id: String, updates: Map<String, Any?>) {
        try {
            usersCollection
                .document(id)
                .set(updates, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.e("UserRepository", "update() failed", e)
            throw e
        }
    }

    /**
     * Deletes a user document by UID.
     */
    override suspend fun delete(id: String) {
        try {
            usersCollection
                .document(id)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e("UserRepository", "delete() failed", e)
            throw e
        }
    }

    /**
     * Returns the currently authenticated user's UID, or null if none.
     */
    fun currentUserId(): String? =
        firebaseAuth.currentUser?.uid
}
