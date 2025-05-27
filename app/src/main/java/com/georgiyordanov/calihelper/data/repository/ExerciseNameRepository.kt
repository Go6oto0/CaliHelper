package com.georgiyordanov.calihelper.data.repository

import android.util.Log
import com.georgiyordanov.calihelper.data.models.ExerciseName
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseNameRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : IRepository<ExerciseName> {

    private val exerciseNamesCollection: CollectionReference
        get() = firestore.collection("exerciseNames")

    /** Creates a new ExerciseName document with an auto-generated ID. */
    override suspend fun create(entity: ExerciseName) {
        try {
            val docRef = exerciseNamesCollection.document()
            docRef.set(entity).await()
        } catch (e: Exception) {
            Log.e("ExerciseNameRepo", "create() failed", e)
            throw e
        }
    }

    /** Reads an ExerciseName by document ID, or returns null if not found. */
    override suspend fun read(id: String): ExerciseName? {
        return try {
            val snap = exerciseNamesCollection.document(id).get().await()
            if (snap.exists()) {
                snap.toObject(ExerciseName::class.java)?.copy(id = snap.id)
            } else {
                Log.w("ExerciseNameRepo", "read(): exercise $id not found")
                null
            }
        } catch (e: Exception) {
            Log.e("ExerciseNameRepo", "read() failed", e)
            throw e
        }
    }

    /** Reads all ExerciseName documents, populating each one's ID from its document key. */
    override suspend fun readAll(): List<ExerciseName>? {
        return try {
            val list = exerciseNamesCollection
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    doc.toObject(ExerciseName::class.java)
                        ?.copy(id = doc.id)
                }
            Log.d("ExerciseNameRepo", "readAll(): fetched ${list.size} items")
            list
        } catch (e: Exception) {
            Log.e("ExerciseNameRepo", "readAll() failed", e)
            throw e
        }
    }

    /** Applies partial updates to an existing ExerciseName document. */
    override suspend fun update(id: String, updates: Map<String, Any?>) {
        try {
            exerciseNamesCollection.document(id).update(updates).await()
        } catch (e: Exception) {
            Log.e("ExerciseNameRepo", "update() failed", e)
            throw e
        }
    }

    /** Deletes an ExerciseName document by ID. */
    override suspend fun delete(id: String) {
        try {
            exerciseNamesCollection.document(id).delete().await()
        } catch (e: Exception) {
            Log.e("ExerciseNameRepo", "delete() failed", e)
            throw e
        }
    }
}
