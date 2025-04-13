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

    // Existing function, returns details for a workout.
    suspend fun fetchWorkoutDetails(workoutId: String): WorkoutPlan? {
        val workout = workoutPlanRepository.read(workoutId)
        Log.d("WorkoutViewModel", "Fetched workout: $workout")
        val allExercises = workoutExerciseRepository.readAll() ?: emptyList()
        Log.d("WorkoutViewModel", "Found ${allExercises.size} exercises in total")
        return workout?.copy(exercises = allExercises.filter { it.workoutPlanId == workout.id })
    }


    // New: Fetch workout by ID and update currentWorkout.
    fun fetchWorkoutById(workoutId: String) {
        viewModelScope.launch {
            Log.d("WorkoutViewModel", "fetchWorkoutById called with id: $workoutId")
            _workoutState.value = WorkoutState.Loading
            val workout = fetchWorkoutDetails(workoutId)
            if (workout != null) {
                currentWorkout.value = workout
                _workoutState.value = WorkoutState.Success
            } else {
                Log.e("WorkoutViewModel", "Workout not found for id: $workoutId")
                _workoutState.value = WorkoutState.Error("Workout not found")
            }
        }
    }

    fun deleteWorkoutPlan(workoutId: String) {
        viewModelScope.launch {
            _workoutState.value = WorkoutState.Loading
            try {
                // Fetch all workout exercises.
                val allExercises = workoutExerciseRepository.readAll() ?: emptyList()
                // Filter exercises related to the workout plan.
                val exercisesToDelete = allExercises.filter { it.workoutPlanId == workoutId }
                // Delete each related exercise.
                exercisesToDelete.forEach { ex ->
                    workoutExerciseRepository.delete(ex.id)
                }
                // Delete the workout plan document.
                workoutPlanRepository.delete(workoutId)
                _workoutState.value = WorkoutState.Success
            } catch (e: Exception) {
                Log.e("WorkoutViewModel", "Failed to delete workout plan", e)
                _workoutState.value = WorkoutState.Error(e.message)
            }
        }
    }

    // Update workout plan for editing.
    fun updateWorkoutPlan(userId: String, workoutId: String, name: String, description: String, exercises: List<WorkoutExercise>) {
        viewModelScope.launch {
            _workoutState.value = WorkoutState.Loading
            try {
                // Update workout plan properties.
                val updates = mapOf(
                    "name" to name,
                    "description" to description
                )
                workoutPlanRepository.update(workoutId, updates)

                // Update exercises:
                // Delete all existing exercises linked to this workout and then re-create them.
                val existingExercises = workoutExerciseRepository.readAll()?.filter { it.workoutPlanId == workoutId } ?: emptyList()
                existingExercises.forEach { ex ->
                    workoutExerciseRepository.delete(ex.id)
                }
                exercises.forEach { exercise ->
                    val updatedExercise = exercise.copy(workoutPlanId = workoutId)
                    workoutExerciseRepository.create(updatedExercise)
                }

                _workoutState.value = WorkoutState.Success
            } catch (e: Exception) {
                Log.e("WorkoutViewModel", "Failed to update workout plan", e)
                _workoutState.value = WorkoutState.Error(e.message)
            }
        }
    }

    // Original update function.
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
                // Generate a new doc reference to get the auto-generated id.
                val docRef = FirebaseFirestore.getInstance()
                    .collection("workoutPlans")
                    .document()
                val workoutPlanId = docRef.id

                // Create the WorkoutPlan model with the generated id.
                val workoutPlan = WorkoutPlan(
                    id = workoutPlanId,
                    userId = userId,
                    name = name,
                    description = description,
                    exercises = emptyList() // Exercises are stored separately.
                )

                // Save the workout plan.
                workoutPlanRepository.create(workoutPlan)

                // For each workout exercise, update the workoutPlanId and then save.
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
