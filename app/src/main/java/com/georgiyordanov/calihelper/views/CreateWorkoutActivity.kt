package com.georgiyordanov.calihelper.views

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.georgiyordanov.calihelper.data.models.WorkoutExercise
import com.georgiyordanov.calihelper.databinding.ActivityCreateWorkoutBinding
import com.georgiyordanov.calihelper.databinding.DialogAddExerciseBinding
import com.georgiyordanov.calihelper.ui.theme.viewmodels.WorkoutState
import com.georgiyordanov.calihelper.ui.theme.viewmodels.WorkoutViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CreateWorkoutActivity : BasicActivity() {

    private lateinit var binding: ActivityCreateWorkoutBinding
    private val workoutViewModel: WorkoutViewModel by viewModels()

    private val exerciseList = mutableListOf<WorkoutExercise>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateWorkoutBinding.inflate(layoutInflater)
        basicBinding.contentFrame.addView(binding.root)

        setupObservers()
        setupListeners()

        workoutViewModel.fetchExerciseNames()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            workoutViewModel.workoutState.collectLatest { state ->
                binding.progressBar.visibility = if (state is WorkoutState.Loading) View.VISIBLE else View.GONE

                when (state) {
                    is WorkoutState.Success -> {
                        Toast.makeText(this@CreateWorkoutActivity, "Workout created successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    is WorkoutState.Error -> {
                        Toast.makeText(this@CreateWorkoutActivity, state.message ?: "An error occurred", Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnAddExercise.setOnClickListener { showAddExerciseDialog() }

        binding.btnCreateWorkout.setOnClickListener {
            val name = binding.etWorkoutName.text.toString().trim()
            val description = binding.etWorkoutDescription.text.toString().trim()

            if (name.isEmpty()) {
                binding.etWorkoutName.error = "Enter workout name"
                return@setOnClickListener
            }

            workoutViewModel.createWorkoutPlan(getUserId(), name, description, exerciseList)
        }
    }

    private fun showAddExerciseDialog() {
        val dialogBinding = DialogAddExerciseBinding.inflate(layoutInflater)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            workoutViewModel.exerciseNames.value.map { it.name }
        )
        dialogBinding.autoCompleteExercise.setAdapter(adapter)

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

                val sets = dialogBinding.etSets.text.toString().toIntOrNull()
                val reps = dialogBinding.etReps.text.toString().toIntOrNull()

                if (sets == null || reps == null) {
                    Toast.makeText(this, "Please enter valid sets and reps", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val workoutExercise = WorkoutExercise(
                    workoutPlanId = "",
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
            val text = exerciseList.joinToString("\n") { exercise ->
                val name = workoutViewModel.exerciseNames.value.find { it.id == exercise.exerciseId }?.name ?: "Unknown"
                "$name - ${exercise.sets} sets, ${exercise.repetitions} reps"
            }
            binding.tvAddedExercises.text = text
        }
    }

    private fun getUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: "defaultUser"
    }
}
