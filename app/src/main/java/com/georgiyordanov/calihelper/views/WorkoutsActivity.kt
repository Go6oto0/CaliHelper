package com.georgiyordanov.calihelper.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.georgiyordanov.calihelper.databinding.ActivityWorkoutsBinding
import com.georgiyordanov.calihelper.ui.theme.viewmodels.WorkoutState
import com.georgiyordanov.calihelper.ui.theme.viewmodels.WorkoutViewModel
import com.google.firebase.auth.FirebaseAuth
import com.georgiyordanov.calihelper.views.adapters.WorkoutAdapter
import kotlinx.coroutines.flow.collectLatest

class WorkoutsActivity : BasicActivity() {

    private lateinit var binding: ActivityWorkoutsBinding
    private val workoutViewModel: WorkoutViewModel by viewModels()
    private lateinit var workoutAdapter: WorkoutAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkoutsBinding.inflate(layoutInflater)
        basicBinding.contentFrame.addView(binding.root)

        // Initialize and attach the adapter.
        workoutAdapter = WorkoutAdapter(
            emptyList(),
            onEdit = { workout ->
                startActivity(Intent(this, EditWorkoutActivity::class.java).apply {
                    putExtra("WORKOUT_ID", workout.id)
                })
            },
            onDelete = { workout ->
                // For example:
                // showDeleteConfirmation(workout)
            }
        )
        binding.recyclerWorkouts.adapter = workoutAdapter
        binding.recyclerWorkouts.layoutManager = LinearLayoutManager(this)

        setupObservers()
        setupListeners()

        // Get the current user's id as a String.
        val userId = getUserId()

        // Fetch both user workouts and exercise names.
        workoutViewModel.fetchUserWorkouts(userId)
        workoutViewModel.fetchExerciseNames()
    }
    override fun onResume() {
        super.onResume()
        val userId = getUserId()
        workoutViewModel.fetchUserWorkouts(userId)
    }


    private fun setupObservers() {
        // Observe workouts list and update RecyclerView.
        lifecycleScope.launchWhenStarted {
            workoutViewModel.workouts.collectLatest { workouts ->
                if (workouts.isEmpty()) {
                    binding.tvNoWorkouts.visibility = View.VISIBLE
                    binding.recyclerWorkouts.visibility = View.GONE
                } else {
                    binding.tvNoWorkouts.visibility = View.GONE
                    binding.recyclerWorkouts.visibility = View.VISIBLE
                    workoutAdapter.submitList(workouts)
                }
            }
        }

        // Observe workout state to show/hide progress bar.
        lifecycleScope.launchWhenStarted {
            workoutViewModel.workoutState.collectLatest { state ->
                when (state) {
                    is WorkoutState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is WorkoutState.Success -> {
                        binding.progressBar.visibility = View.GONE
                    }
                    is WorkoutState.Error -> {
                        binding.progressBar.visibility = View.GONE
                    }
                    else -> {
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }

        // Observe exercise names and update adapter's reference.
        lifecycleScope.launchWhenStarted {
            workoutViewModel.exerciseNames.collectLatest { exerciseNamesList ->
                workoutAdapter.updateExerciseNames(exerciseNamesList)
            }
        }
    }

    private fun setupListeners() {
        binding.btnCreateWorkout.setOnClickListener {
            startActivity(Intent(this, CreateWorkoutActivity::class.java))
        }
    }

    private fun getUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: "defaultUser"
    }
}
