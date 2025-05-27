// AuthViewModel.kt
package com.georgiyordanov.calihelper.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.georgiyordanov.calihelper.data.models.User
import com.georgiyordanov.calihelper.data.repository.AuthRepository
import com.georgiyordanov.calihelper.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.signUp(email, password)
                .onSuccess { fbUser ->
                    val appUser = User(
                        uid = fbUser.uid,
                        email = fbUser.email!!,
                        profileSetup = false
                    )
                    runCatching {
                        Log.d("AuthViewModel", "Creating app user: $appUser")
                        userRepository.create(appUser)
                    }.fold(
                        onSuccess = { _authState.value = AuthState.Success },
                        onFailure = { _authState.value = AuthState.Error(it.message) }
                    )
                }
                .onFailure { _authState.value = AuthState.Error(it.message) }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.signIn(email, password)
                .onSuccess { _authState.value = AuthState.Success }
                .onFailure { _authState.value = AuthState.Error(it.message) }
        }
    }

    fun logout() {
        authRepository.signOut()
        _authState.value = AuthState.Idle
    }

    fun isUserAdmin(): Boolean =
        authRepository.getCurrentUser()?.email
            ?.equals("georgiyordanov_17b@schoolmath.eu", ignoreCase = true)
            ?: false

    fun isUserLoggedIn(): Boolean =
        authRepository.getCurrentUser() != null
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String?) : AuthState()
}
