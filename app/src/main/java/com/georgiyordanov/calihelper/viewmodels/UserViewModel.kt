package com.georgiyordanov.calihelper.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.georgiyordanov.calihelper.data.models.User
import com.georgiyordanov.calihelper.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _userState = MutableStateFlow<UserState>(UserState.Idle)
    val userState: StateFlow<UserState> = _userState.asStateFlow()

    fun createUser(user: User) {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            runCatching {
                userRepository.create(user)
            }.onSuccess {
                _userState.value = UserState.Success
            }.onFailure {
                _userState.value = UserState.Error(it.message)
            }
        }
    }

    fun readUser(id: String) {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            runCatching {
                userRepository.read(id)
            }.onSuccess { user ->
                _userState.value = UserState.UserData(user)
            }.onFailure {
                _userState.value = UserState.Error(it.message)
            }
        }
    }

    fun readAllUsers() {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            runCatching {
                userRepository.readAll()
            }.onSuccess { list ->
                _userState.value = UserState.UsersList(list)
            }.onFailure {
                _userState.value = UserState.Error(it.message)
            }
        }
    }

    fun updateUser(id: String, updates: Map<String, Any?>) {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            runCatching {
                userRepository.update(id, updates)
            }.onSuccess {
                _userState.value = UserState.Success
            }.onFailure {
                _userState.value = UserState.Error(it.message)
            }
        }
    }

    fun deleteUser(id: String) {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            runCatching {
                userRepository.delete(id)
            }.onSuccess {
                _userState.value = UserState.Success
            }.onFailure {
                _userState.value = UserState.Error(it.message)
            }
        }
    }

    fun cascadeDeleteUserData(userId: String, onComplete: (Throwable?) -> Unit) {
        // Use viewModelScope so Hilt disposes properly
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val batch: WriteBatch = firestore.batch()

                // 1) delete the user doc
                batch.delete(firestore.collection("users").document(userId))

                // 2) delete calorieLogs
                val calLogs = firestore.collection("calorieLogs")
                    .whereEqualTo("userId", userId)
                    .get().await()
                calLogs.documents.forEach { batch.delete(it.reference) }

                // 3) delete workoutPlans + exercises
                val plans = firestore.collection("workoutPlans")
                    .whereEqualTo("userId", userId)
                    .get().await()
                for (plan in plans.documents) {
                    batch.delete(plan.reference)
                    val exs = firestore.collection("workoutExercises")
                        .whereEqualTo("workoutPlanId", plan.id)
                        .get().await()
                    exs.documents.forEach { batch.delete(it.reference) }
                }

                // 4) delete foodItems
                val items = firestore.collection("foodItems")
                    .whereEqualTo("userId", userId)
                    .get().await()
                items.documents.forEach { batch.delete(it.reference) }

                // commit batch
                batch.commit().await()
                onComplete(null)
            } catch (t: Throwable) {
                onComplete(t)
            }
        }
    }

    fun checkAndUpdateProfileCompleteness(id: String) {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            runCatching {
                val user = userRepository.read(id)
                if (user != null && isProfileComplete(user) && !user.profileSetup) {
                    userRepository.update(id, mapOf("profileSetup" to true))
                }
            }.onSuccess {
                _userState.value = UserState.Success
            }.onFailure {
                _userState.value = UserState.Error(it.message)
            }
        }
    }

    private fun isProfileComplete(user: User): Boolean =
        !user.userName.isNullOrEmpty() &&
                user.weight  != null &&
                user.height  != null &&
                user.age     != null &&
                !user.goal.isNullOrEmpty()


}

sealed class UserState {
    object Idle : UserState()
    object Loading : UserState()
    object Success : UserState()
    data class Error(val message: String?) : UserState()
    data class UserData(val user: User?) : UserState()
    data class UsersList(val users: List<User>?) : UserState()
}