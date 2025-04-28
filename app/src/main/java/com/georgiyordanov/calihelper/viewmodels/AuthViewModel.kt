package com.georgiyordanov.calihelper.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.georgiyordanov.calihelper.data.models.User
import com.georgiyordanov.calihelper.data.repository.AuthRepository
import com.georgiyordanov.calihelper.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = authRepository.signUp(email, password)
            if (result.isSuccess) {
                val fbUser = result.getOrNull()!!
                val appUser = User(
                    uid = fbUser.uid,
                    email = fbUser.email,       // non-null after successful sign-up
                    profileSetup = false
                )

                try {
                    Log.d("APPUSER_BEFORE_CREATE", appUser.toString())
                    userRepository.create(appUser)    // full write
                    _authState.value = AuthState.Success
                } catch (e: Exception) {
                    _authState.value = AuthState.Error(e.message)
                }
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message)
            }
        }
    }




    // AuthViewModel.kt
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            // Call the suspend function and get a Result<Unit>
            val result = authRepository.signIn(email, password)

            if (result.isSuccess) {
                // credentials were correct
                _authState.value = AuthState.Success
            } else {
                // credentials failed: get the exception message
                val message = result.exceptionOrNull()?.message
                    ?: "Unknown login error"
                _authState.value = AuthState.Error(message)
            }
        }
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }
    fun isUserAdmin(): Boolean {
        val currentUser = authRepository.getCurrentUser()
        // Check for a specific email as a simple example.
        return currentUser?.email?.equals("georgiyordanov_17b@schoolmath.eu", ignoreCase = true) ?: false
    }



    fun isUserLoggedIn(): Boolean = authRepository.getCurrentUser() != null
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String?) : AuthState()
}