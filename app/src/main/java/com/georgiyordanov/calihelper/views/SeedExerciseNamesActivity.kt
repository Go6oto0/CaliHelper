package com.georgiyordanov.calihelper.views

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.georgiyordanov.calihelper.R
import com.georgiyordanov.calihelper.data.models.Exercise
import com.georgiyordanov.calihelper.data.models.ExercisesApiResponse
import com.georgiyordanov.calihelper.databinding.ActivityExerciseSearchBinding
import com.georgiyordanov.calihelper.network.RetrofitInstance
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SeedExerciseNamesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityExerciseSearchBinding
    private val TAG = "ExerciseSearchActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityExerciseSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Fetch all exercises using pagination and seed them into Firestore.
        lifecycleScope.launch {
            try {
                val allExercises = mutableListOf<Exercise>()
                var offset = 0
                val limit = 100 // Maximum allowed per API request.
                while (true) {
                    // Fetch a page of exercises.
                    val response: ExercisesApiResponse = withContext(Dispatchers.IO) {
                        RetrofitInstance.api.getAllExercises(search = "", offset = offset, limit = limit)
                    }
                    val page = response.data.exercises
                    if (page.isEmpty()) break
                    allExercises.addAll(page)
                    // If this page returns fewer than 'limit' exercises, we've reached the end.
                    if (page.size < limit) break
                    offset += limit
                }
                Log.d(TAG, "Fetched exercises list size: ${allExercises.size}")
                seedExerciseNames(allExercises)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching exercises", e)
            }
        }
    }

    /**
     * Persists the fetched exercises to Firestore under the "exerciseNames" collection.
     */
    private fun seedExerciseNames(exercises: List<Exercise>) {
        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()

        exercises.forEach { exercise ->
            // Use the API-provided exerciseId as the document ID.
            val documentId = exercise.exerciseId
            val documentRef = db.collection("exerciseNames").document(documentId)
            val data = mapOf(
                "id" to documentId,
                "name" to exercise.name
            )
            batch.set(documentRef, data)
        }

        batch.commit()
            .addOnSuccessListener {
                Log.d(TAG, "Successfully seeded ${exercises.size} exercises.")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error seeding exercises", e)
            }
    }
}