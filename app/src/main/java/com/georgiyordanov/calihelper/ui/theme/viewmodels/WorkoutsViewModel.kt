package com.georgiyordanov.calihelper.ui.theme.viewmodels

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WorkoutViewModel : ViewModel() {

    private val workoutPlanRepository = WorkoutPlanRepository()
    private val workoutExerciseRepository = WorkoutExerciseRepository()
    private val exerciseNameRepository = ExerciseNameRepository()

    private val _workouts = MutableStateFlow<List<WorkoutPlan>>(emptyList())
    val workouts: StateFlow<List<WorkoutPlan>> = _workouts

    private val _exerciseNames = MutableStateFlow<List<ExerciseName>>(emptyList())
    val exerciseNames: StateFlow<List<ExerciseName>> = _exerciseNames

    val currentWorkout = MutableStateFlow<WorkoutPlan?>(null)

    private val _workoutState = MutableStateFlow<WorkoutState>(WorkoutState.Idle)
    val workoutState: StateFlow<WorkoutState> = _workoutState

    fun fetchExerciseNames() {
        viewModelScope.launch {
            try {
                Log.d("WorkoutViewModel", "Fetching exercise names...")
                _exerciseNames.value = exerciseNameRepository.readAll() ?: emptyList()
            } catch (e: Exception) {
                Log.e("WorkoutViewModel", "Failed to fetch exercise names", e)
                _exerciseNames.value = emptyList()
            }
        }
    }

    fun fetchUserWorkouts(userId: String) {
        viewModelScope.launch {
            _workoutState.value = WorkoutState.Loading
            try {
                val allWorkouts = workoutPlanRepository.readAll() ?: emptyList()
                val allExercises = workoutExerciseRepository.readAll() ?: emptyList()

                val userWorkouts = allWorkouts
                    .filter { it.userId == userId }
                    .map { workout ->
                        val relatedExercises = allExercises.filter { it.workoutPlanId == workout.id }
                        workout.copy(exercises = relatedExercises)
                    }

                _workouts.value = userWorkouts
                _workoutState.value = WorkoutState.Success
            } catch (e: Exception) {
                Log.e("WorkoutViewModel", "Failed to fetch workouts with exercises", e)
                _workoutState.value = WorkoutState.Error(e.message)
            }
        }
    }


    fun fetchWorkoutById(workoutId: String) {
        viewModelScope.launch {
            _workoutState.value = WorkoutState.Loading
            try {
                currentWorkout.value = workoutPlanRepository.read(workoutId)
                _workoutState.value = WorkoutState.Success
            } catch (e: Exception) {
                Log.e("WorkoutViewModel", "Failed to fetch workout by ID", e)
                _workoutState.value = WorkoutState.Error(e.message)
            }
        }
    }

    fun updateWorkoutPlan(workoutId: String, updates: Map<String, Any?>) {
        viewModelScope.launch {
            _workoutState.value = WorkoutState.Loading
            try {
                workoutPlanRepository.update(workoutId, updates)
                _workoutState.value = WorkoutState.Success
            } catch (e: Exception) {
                Log.e("WorkoutViewModel", "Failed to update workout plan", e)
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
                // Generate Firestore ID for the workout plan
                val workoutPlanId = FirebaseFirestore.getInstance()
                    .collection("workoutPlans")
                    .document()
                    .id

                // Create WorkoutPlan model
                val workoutPlan = WorkoutPlan(
                    id = workoutPlanId,
                    userId = userId,
                    name = name,
                    description = description,
                    exercises = emptyList() // Store exercises separately
                )

                // Save the workout plan first
                workoutPlanRepository.create(workoutPlan)

                // Save each exercise with the generated workout ID
                exercises.forEach { exercise ->
                    val linkedExercise = exercise.copy(workoutPlanId = workoutPlanId)
                    workoutExerciseRepository.create(linkedExercise)
                }

                _workoutState.value = WorkoutState.Success
            } catch (e: Exception) {
                Log.e("WorkoutViewModel", "Failed to create workout plan", e)
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
