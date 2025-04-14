package com.georgiyordanov.calihelper.ui.theme.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.georgiyordanov.calihelper.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                authRepository.signUp(email, password) // Assume this throws an exception on failure
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message)
            }
            }
        }


    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                authRepository.signIn(email, password) // Assume this throws an exception on failure
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message)
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