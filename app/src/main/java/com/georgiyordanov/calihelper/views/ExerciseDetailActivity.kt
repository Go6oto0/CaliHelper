package com.georgiyordanov.calihelper.views

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.georgiyordanov.calihelper.data.models.Exercise
import com.georgiyordanov.calihelper.databinding.ActivityExerciseDetailBinding
import com.georgiyordanov.calihelper.network.RetrofitInstance
import kotlinx.coroutines.launch

class ExerciseDetailActivity : BasicActivity() {

    private lateinit var binding: ActivityExerciseDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExerciseDetailBinding.inflate(layoutInflater)
        // Inject the detail layout into the container defined in BasicActivity.
        basicBinding.contentFrame.addView(binding.root)

        // Check if an Exercise object is passed in the intent extras.
        val exerciseFromExtra = intent.getSerializableExtra("EXERCISE_DATA") as? Exercise

        if (exerciseFromExtra != null) {
            updateUI(exerciseFromExtra)
        } else {
            // Fallback: Get the exercise id and fetch details from the API.
            val exerciseId = intent.getStringExtra("EXERCISE_ID") ?: ""
            if (exerciseId.isEmpty()) {
                binding.exerciseTechnique.text = "No exercise id provided."
                return
            }
            lifecycleScope.launch {
                try {
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
        binding.exerciseName.text = exercise.name
        binding.exerciseTechnique.text =
            "Body Parts: ${exercise.bodyParts.joinToString(", ")}\nEquipment: ${exercise.equipments.joinToString(", ")}"
        binding.exerciseInstructions.text = exercise.instructions.joinToString("\n\n")
    }
}
