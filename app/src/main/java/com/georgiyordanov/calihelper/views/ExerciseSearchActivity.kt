package com.georgiyordanov.calihelper.views

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.georgiyordanov.calihelper.data.models.ExerciseName
import com.georgiyordanov.calihelper.databinding.ActivityExerciseSearchBinding
import com.georgiyordanov.calihelper.network.RetrofitInstance
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ExerciseSearchActivity : BasicActivity() {

    private lateinit var binding: ActivityExerciseSearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExerciseSearchBinding.inflate(layoutInflater)
        basicBinding.contentFrame.addView(binding.root)

        // Fetch exercise suggestions from Firestore.
        lifecycleScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val snapshot = db.collection("exerciseNames").get().await()
                val exerciseNames = snapshot.documents.mapNotNull { doc ->
                    val id = doc.getString("id")
                    val name = doc.getString("name")
                    if (id != null && name != null) ExerciseName(id, name) else null
                }
                val adapter = ArrayAdapter(
                    this@ExerciseSearchActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    exerciseNames
                )
                binding.searchBar.threshold = 1
                binding.searchBar.setAdapter(adapter)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Handle selection from the search bar.
        binding.searchBar.setOnItemClickListener { parent, _, position, _ ->
            val selected = parent.getItemAtPosition(position) as ExerciseName
            lifecycleScope.launch {
                try {
                    val response = RetrofitInstance.api.getExerciseById(selected.id)
                    val exercise = response.data
                    val intent = Intent(this@ExerciseSearchActivity, ExerciseDetailActivity::class.java)
                    intent.putExtra("EXERCISE_DATA", exercise)
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
