// WorkoutViewModel.kt
package com.georgiyordanov.calihelper.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.georgiyordanov.calihelper.data.models.ExerciseName
import com.georgiyordanov.calihelper.data.models.WorkoutExercise
import com.georgiyordanov.calihelper.data.models.WorkoutPlan
import com.georgiyordanov.calihelper.data.repository.ExerciseNameRepository
import com.georgiyordanov.calihelper.data.repository.WorkoutExerciseRepository
import com.georgiyordanov.calihelper.data.repository.WorkoutPlanRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val workoutPlanRepository: WorkoutPlanRepository,
    private val workoutExerciseRepository: WorkoutExerciseRepository,
    private val exerciseNameRepository: ExerciseNameRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _workouts = MutableStateFlow<List<WorkoutPlan>>(emptyList())
    val workouts: StateFlow<List<WorkoutPlan>> = _workouts.asStateFlow()

    private val _exerciseNames = MutableStateFlow<List<ExerciseName>>(emptyList())
    val exerciseNames: StateFlow<List<ExerciseName>> = _exerciseNames.asStateFlow()

    private val _currentWorkout = MutableStateFlow<WorkoutPlan?>(null)
    val currentWorkout: StateFlow<WorkoutPlan?> = _currentWorkout.asStateFlow()

    private val _workoutState = MutableStateFlow<WorkoutState>(WorkoutState.Idle)
    val workoutState: StateFlow<WorkoutState> = _workoutState.asStateFlow()

    fun fetchExerciseNames() {
        viewModelScope.launch {
            try {
                val list = exerciseNameRepository.readAll() ?: emptyList()
                _exerciseNames.value = list
                Log.d("WorkoutViewModel", "Exercises: ${list.joinToString { it.name }}")
            } catch (e: Exception) {
                Log.e("WorkoutViewModel", "fetchExerciseNames failed", e)
                _exerciseNames.value = emptyList()
            }
        }
    }

    fun fetchUserWorkouts(userId: String) {
        viewModelScope.launch {
            _workoutState.value = WorkoutState.Loading
            try {
                val plans = workoutPlanRepository.readAll() ?: emptyList()
                val exercises = workoutExerciseRepository.readAll() ?: emptyList()
                val userPlans = plans
                    .filter { it.userId == userId }
                    .map { plan ->
                        plan.copy(
                            exercises = exercises.filter { it.workoutPlanId == plan.id }
                        )
                    }
                _workouts.value = userPlans
                _workoutState.value = WorkoutState.Success
            } catch (e: Exception) {
                Log.e("WorkoutViewModel", "fetchUserWorkouts failed", e)
                _workoutState.value = WorkoutState.Error(e.message)
            }
        }
    }

    suspend fun fetchWorkoutDetails(workoutId: String): WorkoutPlan? {
        val plan = workoutPlanRepository.read(workoutId)
        val allEx = workoutExerciseRepository.readAll() ?: emptyList()
        return plan?.copy(exercises = allEx.filter { it.workoutPlanId == workoutId })
    }

    fun fetchWorkoutById(workoutId: String) {
        viewModelScope.launch {
            _workoutState.value = WorkoutState.Loading
            val plan = fetchWorkoutDetails(workoutId)
            if (plan != null) {
                _currentWorkout.value = plan
                _workoutState.value = WorkoutState.Success
            } else {
                Log.e("WorkoutViewModel", "no workout for $workoutId")
                _workoutState.value = WorkoutState.Error("Not found")
            }
        }
    }

    fun deleteWorkoutPlan(workoutId: String) {
        viewModelScope.launch {
            _workoutState.value = WorkoutState.Loading
            try {
                val allEx = workoutExerciseRepository.readAll() ?: emptyList()
                allEx.filter { it.workoutPlanId == workoutId }
                    .forEach { workoutExerciseRepository.delete(it.id) }
                workoutPlanRepository.delete(workoutId)
                _workoutState.value = WorkoutState.Success
            } catch (e: Exception) {
                Log.e("WorkoutViewModel", "deleteWorkoutPlan failed", e)
                _workoutState.value = WorkoutState.Error(e.message)
            }
        }
    }

    fun updateWorkoutPlan(
        workoutId: String,
        updates: Map<String, Any?>
    ) {
        viewModelScope.launch {
            _workoutState.value = WorkoutState.Loading
            try {
                workoutPlanRepository.update(workoutId, updates)
                _workoutState.value = WorkoutState.Success
            } catch (e: Exception) {
                Log.e("WorkoutViewModel", "updateWorkoutPlan failed", e)
                _workoutState.value = WorkoutState.Error(e.message)
            }
        }
    }

    fun updateWorkoutPlan(
        userId: String,
        workoutId: String,
        name: String,
        description: String,
        exercises: List<WorkoutExercise>
    ) {
        viewModelScope.launch {
            _workoutState.value = WorkoutState.Loading
            try {
                workoutPlanRepository.update(workoutId, mapOf(
                    "name" to name,
                    "description" to description
                ))

                val old = workoutExerciseRepository.readAll()
                    ?.filter { it.workoutPlanId == workoutId } ?: emptyList()
                old.forEach { workoutExerciseRepository.delete(it.id) }

                exercises.forEach { ex ->
                    workoutExerciseRepository.create(ex.copy(workoutPlanId = workoutId))
                }
                _workoutState.value = WorkoutState.Success
            } catch (e: Exception) {
                Log.e("WorkoutViewModel", "updateWorkoutPlan(detailed) failed", e)
                _workoutState.value = WorkoutState.Error(e.message)
            }
        }
    }

    fun createWorkoutPlan(
        userId: String,
        name: String,
        description: String,
        exercises: List<WorkoutExercise>
    ) {
        viewModelScope.launch {
            _workoutState.value = WorkoutState.Loading
            try {
                // generate a new ID
                val docId = firestore.collection("workoutPlans").document().id
                val plan = WorkoutPlan(
                    id = docId,
                    userId = userId,
                    name = name,
                    description = description,
                    exercises = emptyList()
                )
                workoutPlanRepository.create(plan)
                exercises.forEach { ex ->
                    workoutExerciseRepository.create(ex.copy(workoutPlanId = docId))
                }
                _workoutState.value = WorkoutState.Success
            } catch (e: Exception) {
                Log.e("WorkoutViewModel", "createWorkoutPlan failed", e)
                _workoutState.value = WorkoutState.Error(e.message)
            }
        }
    }
}

sealed class WorkoutState {
    object Idle : WorkoutState()
    object Loading : WorkoutState()
    object Success : WorkoutState()
    data class Error(val message: String?) : WorkoutState()
}
