package com.georgiyordanov.calihelper.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.georgiyordanov.calihelper.data.models.ExerciseName
import com.georgiyordanov.calihelper.databinding.ActivityExerciseSearchBinding
import com.georgiyordanov.calihelper.network.RetrofitInstance
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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

            // 1) block further input & show spinner
            binding.searchBar.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE

            lifecycleScope.launch {
                try {
                    val response = RetrofitInstance.api.getExerciseById(selected.id)
                    val exercise = response.data
                    startActivity(
                        Intent(this@ExerciseSearchActivity, ExerciseDetailActivity::class.java)
                            .putExtra("EXERCISE_DATA", exercise)
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(
                        this@ExerciseSearchActivity,
                        "Error loading exercise",
                        Toast.LENGTH_SHORT
                    ).show()
                    // 3) on error, hide spinner & re-enable
                    binding.progressBar.visibility = View.GONE
                    binding.searchBar.isEnabled = true
                }
                // if you want to immediately hide the spinner after startActivity(), you can:
                // binding.progressBar.visibility = View.GONE
                // (the new activity will cover it anyway)
            }
        }

    }
    override fun onResume() {
        super.onResume()
        // hide spinner and re-enable search bar
        binding.progressBar.visibility = View.GONE
        binding.searchBar.isEnabled = true
        // clear whatever text was in the bar
        binding.searchBar.text?.clear()
    }

}
