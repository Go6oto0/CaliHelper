package com.georgiyordanov.calihelper.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.georgiyordanov.calihelper.databinding.ActivityWorkoutsBinding
import com.georgiyordanov.calihelper.ui.theme.viewmodels.WorkoutState
import com.georgiyordanov.calihelper.ui.theme.viewmodels.WorkoutViewModel
import com.georgiyordanov.calihelper.views.adapters.WorkoutAdapter
import com.georgiyordanov.calihelper.data.models.WorkoutPlan
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class WorkoutsActivity : BasicActivity() {

    private lateinit var binding: ActivityWorkoutsBinding
    private val workoutViewModel: WorkoutViewModel by viewModels()
    private lateinit var workoutAdapter: WorkoutAdapter

    // When true, the next WorkoutState.Success corresponds to our pending delete
    private var pendingDelete = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkoutsBinding.inflate(layoutInflater)
        basicBinding.contentFrame.addView(binding.root)

        // 1) RecyclerView + adapter
        workoutAdapter = WorkoutAdapter(
            workouts = emptyList(),
            onEdit   = { workout ->
                startActivity(Intent(this, EditWorkoutActivity::class.java)
                    .apply { putExtra("WORKOUT_ID", workout.id) })
            },
            onDelete = { workout -> confirmDelete(workout) }
        )
        binding.recyclerWorkouts.apply {
            adapter = workoutAdapter
            layoutManager = LinearLayoutManager(this@WorkoutsActivity)
        }

        // 2) Observe the flows
        setupObservers()

        // 3) “Create” button
        binding.btnCreateWorkout.setOnClickListener {
            startActivity(Intent(this, CreateWorkoutActivity::class.java))
        }

        // 4) Initial load
        refreshWorkouts()
        workoutViewModel.fetchExerciseNames()
    }

    override fun onResume() {
        super.onResume()
        // In case we returned from edit/create
        refreshWorkouts()
    }

    private fun setupObservers() {
        // A) Workouts list → adapter
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                workoutViewModel.workouts.collectLatest { list ->
                    if (list.isEmpty()) {
                        binding.tvNoWorkouts.visibility = View.VISIBLE
                        binding.recyclerWorkouts.visibility = View.GONE
                    } else {
                        binding.tvNoWorkouts.visibility = View.GONE
                        binding.recyclerWorkouts.visibility = View.VISIBLE
                        workoutAdapter.submitList(list)
                    }
                }
            }
        }

        // B) Loading / error and delete‑completion hook
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                workoutViewModel.workoutState.collectLatest { state ->
                    // show/hide progress
                    binding.progressBar.visibility =
                        if (state is WorkoutState.Loading) View.VISIBLE else View.GONE

                    // if we were waiting on a delete, and now it's success...
                    if (pendingDelete && state is WorkoutState.Success) {
                        pendingDelete = false
                        refreshWorkouts()
                    }
                }
            }
        }

        // C) Exercise names → adapter
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                workoutViewModel.exerciseNames.collectLatest {
                    workoutAdapter.updateExerciseNames(it)
                }
            }
        }
    }

    private fun refreshWorkouts() {
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            workoutViewModel.fetchUserWorkouts(uid)
        }
    }

    private fun confirmDelete(workout: WorkoutPlan) {
        AlertDialog.Builder(this)
            .setTitle("Delete “${workout.name}”?")
            .setMessage("Are you sure you want to delete this workout?")
            .setPositiveButton("Delete") { dialog, _ ->
                dialog.dismiss()
                // mark pending, then invoke delete
                pendingDelete = true
                workoutViewModel.deleteWorkoutPlan(workout.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
