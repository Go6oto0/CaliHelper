package com.georgiyordanov.calihelper.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()
    suspend fun signUp(email: String, password: String): Result<Unit> {
        return try {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: FirebaseAuthException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<Unit> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: FirebaseAuthException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    fun getCurrentUser() = firebaseAuth.currentUser
}