package com.georgiyordanov.calihelper.views

import android.os.Bundle
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.georgiyordanov.calihelper.R
import com.georgiyordanov.calihelper.data.models.ExerciseName
import com.georgiyordanov.calihelper.data.models.WorkoutPlan
import com.georgiyordanov.calihelper.databinding.ActivityWorkoutSuggestorBinding
import com.georgiyordanov.calihelper.views.adapters.WorkoutAdapter
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WorkoutSuggestorActivity : BasicActivity() {

    private lateinit var binding: ActivityWorkoutSuggestorBinding
    private lateinit var adapter: WorkoutAdapter
    private val db = FirebaseFirestore.getInstance()

    // Currently selected filter
    private var selectedType: String = "pull"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkoutSuggestorBinding.inflate(layoutInflater)
        basicBinding.contentFrame.addView(binding.root)

        // edge‑to‑edge
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        setupFilters()
        setupRecycler()
        loadSuggestions()
    }

    private fun setupFilters() {
        // Default-select the “Pull” chip
        binding.chipGroupWorkoutType.check(R.id.chipPull)
        selectedType = "pull"
        loadSuggestions()

        // Listen for chip changes
        binding.chipGroupWorkoutType.setOnCheckedChangeListener { _, checkedId ->
            selectedType = when (checkedId) {
                R.id.chipPull     -> "pull"
                R.id.chipPush     -> "push"
                R.id.chipLegs     -> "legs"
                R.id.chipCore     -> "core"
                R.id.chipFullBody -> "fullbody"
                else              -> "pull"  // fallback
            }
            loadSuggestions()
        }
    }


    private fun setupRecycler() {
        adapter = WorkoutAdapter(
            workouts      = emptyList(),
            exerciseNames = emptyList()
            // no onEdit/onDelete → buttons hidden
        )
        binding.rvWorkouts.apply {
            layoutManager = LinearLayoutManager(this@WorkoutSuggestorActivity)
            adapter = this@WorkoutSuggestorActivity.adapter
        }
    }

    private fun loadSuggestions() {
        db.collection("workoutPlanSuggestions")
            .whereEqualTo("type", selectedType)
            .get()
            .addOnSuccessListener { snap ->
                // 1) Deserialize plans
                val plans: List<WorkoutPlan> = snap.toObjects(WorkoutPlan::class.java)
                adapter.submitList(plans)

                // 2) Build a fake lookup table where each ExerciseName.id == name == exerciseId
                val ids = plans
                    .flatMap { it.exercises }
                    .map { it.exerciseId }
                    .distinct()
                val fakeLookup = ids.map { exId ->
                    ExerciseName(id = exId, name = exId)
                }

                // 3) Update adapter with that lookup
                adapter.updateExerciseNames(fakeLookup)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error loading workouts: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
