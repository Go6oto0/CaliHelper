package com.georgiyordanov.calihelper.data.repository
import android.util.Log
import com.georgiyordanov.calihelper.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await


class UserRepository : IRepository<User> {
    private var db = FirebaseFirestore.getInstance()
    private var usersCollection = db.collection("users")
    private val firebaseAuth = FirebaseAuth.getInstance()
    override suspend fun create(entity: User) {
        try {
            Log.d("ENTITY_IN_CREATE", entity.toString())
            Log.d("CREATE_CALLER", entity.toString(), Throwable("Stack trace"))
            val documentRef = usersCollection.document(entity.uid)
            documentRef.set(entity).await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e // Propagate the exception to the caller
        }
    }

    override suspend fun read(id: String): User? {
        return try {
            val document = usersCollection.document(id).get().await()
            if (document.exists()) {
                document.toObject(User::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e // Propagate the exception to the caller
        }
    }

    override suspend fun readAll(): List<User>? {
        return try {
            val snapshot = usersCollection.get().await()
            snapshot.toObjects(User::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e // Propagate the exception to the caller
        }
    }

    override suspend fun update(id: String, updates: Map<String, Any?>) {
        try {
            usersCollection.document(id).set(updates, SetOptions.merge()).await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e // Propagate the exception to the caller
        }
    }

    override suspend fun delete(id: String) {
        try {
            usersCollection.document(id).delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e // Propagate the exception to the caller
        }
    }

}