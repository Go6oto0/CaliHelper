package com.georgiyordanov.calihelper.views

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.georgiyordanov.calihelper.data.models.WorkoutExercise
import com.georgiyordanov.calihelper.data.models.WorkoutPlan
import com.georgiyordanov.calihelper.databinding.ActivityEditWorkoutBinding
import com.georgiyordanov.calihelper.databinding.DialogAddExerciseBinding
import com.georgiyordanov.calihelper.ui.theme.viewmodels.WorkoutState
import com.georgiyordanov.calihelper.ui.theme.viewmodels.WorkoutViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EditWorkoutActivity : BasicActivity() {

    private lateinit var binding: ActivityEditWorkoutBinding
    private val workoutViewModel: WorkoutViewModel by viewModels()

    // The currently loaded workout.
    private var currentWorkout: WorkoutPlan? = null
    // In-memory list to collect and update exercises.
    private val exerciseList = mutableListOf<WorkoutExercise>()
    // Store the document id passed via the intent.
    private var workoutDocId: String? = null
    // Flag to check if update was triggered.
    private var isUpdating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditWorkoutBinding.inflate(layoutInflater)
        basicBinding.contentFrame.addView(binding.root)

        setupObservers()
        setupListeners()

        // Get the workout id from the intent.
        workoutDocId = intent.getStringExtra("WORKOUT_ID")
        if (workoutDocId.isNullOrEmpty()) {
            Toast.makeText(this, "Workout not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        // Fetch the workout details by its id.
        workoutViewModel.fetchWorkoutById(workoutDocId!!)
        // Also, fetch available exercise names.
        workoutViewModel.fetchExerciseNames()
    }

    private fun setupObservers() {
        // Observe the current workout so that fields can be pre-populated.
        lifecycleScope.launch {
            workoutViewModel.currentWorkout.collectLatest { workout ->
                if (workout != null) {
                    currentWorkout = workout
                    Log.d("EditWorkoutActivity", "Loaded workout: $workout")
                    binding.etWorkoutName.setText(workout.name)
                    binding.etWorkoutDescription.setText(workout.description)
                    exerciseList.clear()
                    exerciseList.addAll(workout.exercises)
                    updateExerciseListUI()
                } else {
                    Log.d("EditWorkoutActivity", "No workout loaded yet.")
                }
            }
        }

        // Observe the workout state to show/hide a progress bar or handle errors.
        lifecycleScope.launch {
            workoutViewModel.workoutState.collectLatest { state ->
                when (state) {
                    is WorkoutState.Loading -> {
                        binding.progressBar.visibility = android.view.View.VISIBLE
                    }
                    is WorkoutState.Success -> {
                        binding.progressBar.visibility = android.view.View.GONE
                        if (isUpdating) {
                            Toast.makeText(
                                this@EditWorkoutActivity,
                                "Workout updated successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                    }
                    is WorkoutState.Error -> {
                        binding.progressBar.visibility = android.view.View.GONE
                        Toast.makeText(
                            this@EditWorkoutActivity,
                            state.message ?: "An error occurred",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> binding.progressBar.visibility = android.view.View.GONE
                }
            }
        }
    }

    private fun setupListeners() {
        // Listener to add an exercise via a dialog.
        binding.btnAddExercise.setOnClickListener {
            showAddExerciseDialog()
        }
        // Listener to update the workout.
        binding.btnUpdateWorkout.setOnClickListener {
            val name = binding.etWorkoutName.text.toString().trim()
            val description = binding.etWorkoutDescription.text.toString().trim()

            if (name.isEmpty()) {
                binding.etWorkoutName.error = "Enter workout name"
                return@setOnClickListener
            }
            val userId = getUserId()

            // Use the workoutDocId from the intent instead of currentWorkout?.id.
            val currentId = workoutDocId
            if (currentId.isNullOrEmpty()) {
                Toast.makeText(this, "Workout ID not available", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            isUpdating = true

            // Create an updated WorkoutPlan, preserving the document ID.
            val updatedWorkout = WorkoutPlan(
                id = currentId,
                userId = userId,
                name = name,
                description = description,
                exercises = exerciseList
            )
            Log.d("EditWorkoutActivity", "Updating workout: $updatedWorkout")
            workoutViewModel.updateWorkoutPlan(currentId, updatedWorkout.toMap())
        }
    }

    private fun showAddExerciseDialog() {
        val dialogBinding = DialogAddExerciseBinding.inflate(layoutInflater)

        // Update the AutoCompleteTextView with exercise names.
        lifecycleScope.launch {
            workoutViewModel.exerciseNames.collect { exerciseNamesList ->
                val adapter = ArrayAdapter(
                    this@EditWorkoutActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    exerciseNamesList
                )
                dialogBinding.autoCompleteExercise.setAdapter(adapter)
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Exercise")
            .setView(dialogBinding.root)
            .setPositiveButton("Add") { dialogInterface, _ ->
                val selectedText = dialogBinding.autoCompleteExercise.text.toString().trim()
                val exerciseName = workoutViewModel.exerciseNames.value.find {
                    it.name.equals(selectedText, ignoreCase = true)
                }
                if (exerciseName == null) {
                    Toast.makeText(this, "Please select a valid exercise", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val setsText = dialogBinding.etSets.text.toString().trim()
                val repsText = dialogBinding.etReps.text.toString().trim()
                if (setsText.isEmpty() || repsText.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val sets = setsText.toIntOrNull() ?: 0
                val reps = repsText.toIntOrNull() ?: 0

                val workoutExercise = WorkoutExercise(
                    workoutPlanId = "", // Not used in this context.
                    exerciseId = exerciseName.id,
                    repetitions = reps,
                    sets = sets
                )
                exerciseList.add(workoutExercise)
                updateExerciseListUI()
                Toast.makeText(this, "${exerciseName.name} added", Toast.LENGTH_SHORT).show()
                dialogInterface.dismiss()
            }
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()
        dialog.show()
    }

    private fun updateExerciseListUI() {
        if (exerciseList.isEmpty()) {
            binding.tvAddedExercises.text = "No exercises added."
        } else {
            val sb = StringBuilder()
            exerciseList.forEach { exercise ->
                val name = workoutViewModel.exerciseNames.value.find {
                    it.id == exercise.exerciseId
                }?.name ?: "Unknown"
                sb.append("$name - ${exercise.sets} sets, ${exercise.repetitions} reps\n")
            }
            binding.tvAddedExercises.text = sb.toString()
        }
    }

    private fun getUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: "defaultUser"
    }
}
