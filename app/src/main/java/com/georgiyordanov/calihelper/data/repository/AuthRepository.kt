package com.georgiyordanov.calihelper.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    suspend fun signUp(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .await()

            val user = authResult.user
                ?: return Result.failure(Exception("Firebase returned null user"))

            Result.success(user)
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

    fun getCurrentUser(): FirebaseUser? =
        firebaseAuth.currentUser

    fun signOut() = firebaseAuth.signOut()
}
