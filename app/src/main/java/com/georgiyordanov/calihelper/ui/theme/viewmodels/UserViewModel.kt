package com.georgiyordanov.calihelper.ui.theme.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.georgiyordanov.calihelper.data.models.User
import com.georgiyordanov.calihelper.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    private val userRepository = UserRepository()

    private val _userState = MutableStateFlow<UserState>(UserState.Idle)
    val userState: StateFlow<UserState> = _userState

    fun createUser(user: User) {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            try {
                userRepository.create(user)
                _userState.value = UserState.Success
            } catch (e: Exception) {
                _userState.value = UserState.Error(e.message)
            }
        }
    }

    fun readUser(id: String) {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            try {
                val user = userRepository.read(id)
                _userState.value = UserState.UserData(user)
            } catch (e: Exception) {
                _userState.value = UserState.Error(e.message)
            }
        }
    }

    fun readAllUsers() {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            try {
                val users = userRepository.readAll()
                _userState.value = UserState.UsersList(users)
            } catch (e: Exception) {
                _userState.value = UserState.Error(e.message)
            }
        }
    }

    fun updateUser(id: String, updates: Map<String, Any?>) {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            try {
                userRepository.update(id, updates)
                _userState.value = UserState.Success
            } catch (e: Exception) {
                _userState.value = UserState.Error(e.message)
            }
        }
    }
    fun isProfileComplete(user: User): Boolean {
        return !user.userName.isNullOrEmpty() &&
                user.weight != null &&
                user.height != null &&
                user.age != null &&
                !user.goal.isNullOrEmpty()
    }
    fun checkAndUpdateProfileCompleteness(id: String) {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            try {
                val user = userRepository.read(id)
                user?.let {
                    // Check if the profile is complete.
                    val complete = isProfileComplete(it)
                    // If it's complete and the flag is not yet set, update the property.
                    if (complete && !it.profileSetup) {
                        val updates = mapOf("profileSetup" to true)
                        userRepository.update(id, updates)
                        // Optionally update local state or re-read the user.
                    }
                }
                _userState.value = UserState.Success
            } catch (e: Exception) {
                _userState.value = UserState.Error(e.message)
            }
        }
    }


    fun deleteUser(id: String) {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            try {
                userRepository.delete(id)
                _userState.value = UserState.Success
            } catch (e: Exception) {
                _userState.value = UserState.Error(e.message)
            }
        }
    }
    suspend fun isCurrentUserProfileSetup(): Boolean {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        return if (firebaseUser != null) {
            try {
                // Retrieve the user record from your database using the Firebase user's UID.
                val user: User? = userRepository.read(firebaseUser.uid)
                // Check the user's isProfileSetup property. If the user record is not found, assume false.
                user?.profileSetup ?: false
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }
    }
}

sealed class UserState {
    object Idle : UserState()
    object Loading : UserState()
    object Success : UserState()
    data class Error(val message: String?) : UserState()
    data class UserData(val user: User?) : UserState()
    data class UsersList(val users: List<User>?) : UserState()
}
