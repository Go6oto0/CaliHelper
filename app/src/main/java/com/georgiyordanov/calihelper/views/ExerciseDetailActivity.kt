package com.georgiyordanov.calihelper.views

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import com.georgiyordanov.calihelper.R
import com.georgiyordanov.calihelper.data.models.Exercise
import com.georgiyordanov.calihelper.databinding.ActivityExerciseDetailBinding
import com.georgiyordanov.calihelper.network.RetrofitInstance
import kotlinx.coroutines.launch

class ExerciseDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityExerciseDetailBinding
    private val TAG = "ExerciseDetailActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityExerciseDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup hamburger button.
        binding.topBarInclude.hamburgerButton.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // Setup navigation view.
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_exercises -> startActivity(Intent(this, ExerciseSearchActivity::class.java))
                R.id.nav_calorie_tracker -> startActivity(Intent(this, CalorieTrackerActivity::class.java))
                R.id.nav_workouts -> startActivity(Intent(this, WorkoutsActivity::class.java))
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Try to retrieve the full Exercise object from intent extras.
        val exerciseFromExtra = intent.getSerializableExtra("EXERCISE_DATA") as? Exercise

        if (exerciseFromExtra != null) {
            updateUI(exerciseFromExtra)
        } else {
            // Fallback: Get the exercise id from intent extras and fetch from API.
            val exerciseId = intent.getStringExtra("EXERCISE_ID") ?: ""
            if (exerciseId.isEmpty()) {
                binding.exerciseTechnique.text = "No exercise id provided."
                return
            }

            lifecycleScope.launch {
                try {
                    // Call the API; getExerciseById returns a wrapper with .data containing the Exercise.
                    val response = RetrofitInstance.api.getExerciseById(exerciseId)
                    val fetchedExercise: Exercise = response.data
                    updateUI(fetchedExercise)
                } catch (e: Exception) {
                    e.printStackTrace()
                    binding.exerciseTechnique.text = "Error loading exercise details."
                    binding.exerciseInstructions.text = ""
                }
            }
        }
    }

    private fun updateUI(exercise: Exercise) {
        // Set the exercise name.
        binding.exerciseName.text = exercise.name

        // Display technique details using body parts and equipments.
        binding.exerciseTechnique.text =
            "Body Parts: ${exercise.bodyParts.joinToString(", ")}\nEquipment: ${exercise.equipments.joinToString(", ")}"

        // Combine instructions into a single string.
        binding.exerciseInstructions.text = exercise.instructions.joinToString("\n\n")
    }
}
