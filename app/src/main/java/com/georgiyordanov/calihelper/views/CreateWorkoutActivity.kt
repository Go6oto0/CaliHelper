package com.georgiyordanov.calihelper.views

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.georgiyordanov.calihelper.adapters.WorkoutExerciseAdapter
import com.georgiyordanov.calihelper.data.models.WorkoutExercise
import com.georgiyordanov.calihelper.databinding.ActivityCreateWorkoutBinding
import com.georgiyordanov.calihelper.databinding.DialogAddExerciseBinding
import com.georgiyordanov.calihelper.viewmodels.WorkoutState
import com.georgiyordanov.calihelper.viewmodels.WorkoutViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateWorkoutActivity : BasicActivity() {

    private lateinit var binding: ActivityCreateWorkoutBinding
    private val workoutViewModel: WorkoutViewModel by viewModels()

    private val exerciseList = mutableListOf<WorkoutExercise>()
    private lateinit var exerciseAdapter: WorkoutExerciseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateWorkoutBinding.inflate(layoutInflater)
        basicBinding.contentFrame.addView(binding.root)

        // Initialize RecyclerView and adapter.
        exerciseAdapter = WorkoutExerciseAdapter(
            items = exerciseList,
            onRemoveClick = { position ->
                exerciseList.removeAt(position)
                exerciseAdapter.notifyItemRemoved(position)
            },
            onItemClick = { position ->
                showEditExerciseDialog(position)
            },
            nameLookup = { id ->
                workoutViewModel.exerciseNames.value.find { it.id == id }?.name ?: "Unknown"
            }
        )

        binding.rvExercises.layoutManager = LinearLayoutManager(this)
        binding.rvExercises.adapter = exerciseAdapter

        setupObservers()
        setupListeners()

        workoutViewModel.fetchExerciseNames()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            workoutViewModel.workoutState.collectLatest { state ->
                binding.progressBar.visibility =
                    if (state is WorkoutState.Loading) android.view.View.VISIBLE
                    else android.view.View.GONE

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
            val name = binding.etCreateWorkoutName.text.toString().trim()
            val description = binding.etCreateWorkoutDescription.text.toString().trim()

            // Validate name
            if (name.isEmpty()) {
                binding.tilCreateName.error = "Enter workout name"
                return@setOnClickListener
            } else {
                binding.tilCreateName.error = null
            }

            // All good, create the workout
            workoutViewModel.createWorkoutPlan(getUserId(), name, description, exerciseList)
        }

    }

    private fun showAddExerciseDialog() {
        val dialogBinding = DialogAddExerciseBinding.inflate(layoutInflater)

        val autoCompleteAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            workoutViewModel.exerciseNames.value.map { it.name }
        )
        dialogBinding.autoCompleteExercise.setAdapter(autoCompleteAdapter)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Exercise")
            .setView(dialogBinding.root)
            .setPositiveButton("Add") { dialogInterface, _ ->
                val selectedText = dialogBinding.autoCompleteExercise.text.toString().trim()
                val exerciseNameObj = workoutViewModel.exerciseNames.value.find {
                    it.name.equals(selectedText, ignoreCase = true)
                }
                if (exerciseNameObj == null) {
                    Toast.makeText(this, "Please select a valid exercise", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val sets = dialogBinding.etSets.text.toString().toIntOrNull()
                val reps = dialogBinding.etReps.text.toString().toIntOrNull()
                if (sets == null || reps == null) {
                    Toast.makeText(this, "Please enter valid sets and reps", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Create and add new workout exercise.
                val workoutExercise = WorkoutExercise(
                    workoutPlanId = "",
                    exerciseId = exerciseNameObj.id,
                    repetitions = reps,
                    sets = sets
                )
                exerciseList.add(workoutExercise)
                exerciseAdapter.notifyItemInserted(exerciseList.size - 1)
                Toast.makeText(this, "${exerciseNameObj.name} added", Toast.LENGTH_SHORT).show()
                dialogInterface.dismiss()
            }
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()
    }

    // New function to allow editing of an existing exercise.
    private fun showEditExerciseDialog(position: Int) {
        val currentExercise = exerciseList[position]
        // Lookup current exercise name.
        val currentName = workoutViewModel.exerciseNames.value.find { it.id == currentExercise.exerciseId }?.name ?: ""
        val dialogBinding = DialogAddExerciseBinding.inflate(layoutInflater)

        // Pre-fill dialog fields with current values.
        dialogBinding.autoCompleteExercise.setText(currentName, false)
        dialogBinding.etSets.setText(currentExercise.sets.toString())
        dialogBinding.etReps.setText(currentExercise.repetitions.toString())

        val autoCompleteAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            workoutViewModel.exerciseNames.value.map { it.name }
        )
        dialogBinding.autoCompleteExercise.setAdapter(autoCompleteAdapter)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Exercise")
            .setView(dialogBinding.root)
            .setPositiveButton("Save") { dialogInterface, _ ->
                val updatedText = dialogBinding.autoCompleteExercise.text.toString().trim()
                val selectedExercise = workoutViewModel.exerciseNames.value.find {
                    it.name.equals(updatedText, ignoreCase = true)
                }
                if (selectedExercise == null) {
                    Toast.makeText(this, "Please select a valid exercise", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val updatedSets = dialogBinding.etSets.text.toString().toIntOrNull()
                val updatedReps = dialogBinding.etReps.text.toString().toIntOrNull()
                if (updatedSets == null || updatedReps == null) {
                    Toast.makeText(this, "Please enter valid sets and reps", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                // Create a new workout exercise instance with updated values.
                val updatedExercise = WorkoutExercise(
                    workoutPlanId = currentExercise.workoutPlanId,
                    exerciseId = selectedExercise.id,
                    repetitions = updatedReps,
                    sets = updatedSets
                )
                // Update the item in the list.
                exerciseList[position] = updatedExercise
                exerciseAdapter.notifyItemChanged(position)
                Toast.makeText(this, "${selectedExercise.name} updated", Toast.LENGTH_SHORT).show()
                dialogInterface.dismiss()
            }
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun getUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: "defaultUser"
    }
}
