package com.georgiyordanov.calihelper.views

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.georgiyordanov.calihelper.adapters.WorkoutExerciseAdapter
import com.georgiyordanov.calihelper.data.models.WorkoutExercise
import com.georgiyordanov.calihelper.databinding.ActivityEditWorkoutBinding
import com.georgiyordanov.calihelper.databinding.DialogAddExerciseBinding
import com.georgiyordanov.calihelper.ui.theme.viewmodels.WorkoutState
import com.georgiyordanov.calihelper.ui.theme.viewmodels.WorkoutViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EditWorkoutActivity : BasicActivity() {

    private lateinit var binding: ActivityEditWorkoutBinding
    private val workoutViewModel: WorkoutViewModel by viewModels()

    // Holds the workout exercises for editing.
    private val exerciseList = mutableListOf<WorkoutExercise>()
    private lateinit var exerciseAdapter: WorkoutExerciseAdapter

    // Retrieve the workout id from the intent.
    private lateinit var workoutId: String

    // Flag to indicate whether an update was triggered.
    private var updateTriggered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditWorkoutBinding.inflate(layoutInflater)
        basicBinding.contentFrame.addView(binding.root)

        workoutId = intent.getStringExtra("WORKOUT_ID") ?: ""
        if (workoutId.isEmpty()) {
            Toast.makeText(this, "Workout not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

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
                val exercise = workoutViewModel.exerciseNames.value.find { it.id == id }
                exercise?.name ?: "Unknown"
            }
        )
        binding.rvExercises.layoutManager = LinearLayoutManager(this)
        binding.rvExercises.adapter = exerciseAdapter

        // Load workout details first.
        loadWorkoutDetails()

        setupListeners()
        setupObservers()

        // Fetch exercise names if not already loaded.
        workoutViewModel.fetchExerciseNames()
    }

    private fun loadWorkoutDetails() {
        lifecycleScope.launch {
            workoutViewModel.fetchWorkoutById(workoutId)
            // Collect from currentWorkout until a workout is loaded.
            workoutViewModel.currentWorkout.collectLatest { workout ->
                if (workout != null) {
                    binding.etWorkoutName.setText(workout.name)
                    binding.etWorkoutDescription.setText(workout.description)
                    exerciseList.clear()
                    exerciseList.addAll(workout.exercises)
                    exerciseAdapter.notifyDataSetChanged()
                    // We do not finish() here because this is part of the load phase.
                }
            }
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            workoutViewModel.workoutState.collectLatest { state ->
                binding.progressBar.visibility =
                    if (state is WorkoutState.Loading) android.view.View.VISIBLE else android.view.View.GONE
                when (state) {
                    is WorkoutState.Success -> {
                        // Only finish if an update was triggered.
                        if (updateTriggered) {
                            Toast.makeText(this@EditWorkoutActivity, "Workout updated successfully!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                    is WorkoutState.Error -> {
                        Toast.makeText(this@EditWorkoutActivity, state.message ?: "An error occurred", Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnAddExercise.setOnClickListener { showAddExerciseDialog() }

        binding.btnUpdateWorkout.setOnClickListener {
            val updatedName = binding.etWorkoutName.text.toString().trim()
            val updatedDescription = binding.etWorkoutDescription.text.toString().trim()

            if (updatedName.isEmpty()) {
                binding.etWorkoutName.error = "Enter workout name"
                return@setOnClickListener
            }

            // Mark that we are triggering an update.
            updateTriggered = true

            workoutViewModel.updateWorkoutPlan(
                userId = getUserId(),
                workoutId = workoutId,
                name = updatedName,
                description = updatedDescription,
                exercises = exerciseList
            )
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

    private fun showEditExerciseDialog(position: Int) {
        val currentExercise = exerciseList[position]
        val currentName = workoutViewModel.exerciseNames.value.find { it.id == currentExercise.exerciseId }?.name ?: ""
        val dialogBinding = DialogAddExerciseBinding.inflate(layoutInflater)
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
                val updatedExercise = WorkoutExercise(
                    workoutPlanId = currentExercise.workoutPlanId,
                    exerciseId = selectedExercise.id,
                    repetitions = updatedReps,
                    sets = updatedSets
                )
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
