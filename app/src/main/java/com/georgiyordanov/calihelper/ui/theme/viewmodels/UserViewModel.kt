package com.georgiyordanov.calihelper.ui.theme.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.georgiyordanov.calihelper.data.models.User
import com.georgiyordanov.calihelper.data.repository.UserRepository
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
}

sealed class UserState {
    object Idle : UserState()
    object Loading : UserState()
    object Success : UserState()
    data class Error(val message: String?) : UserState()
    data class UserData(val user: User?) : UserState()
    data class UsersList(val users: List<User>?) : UserState()
}
