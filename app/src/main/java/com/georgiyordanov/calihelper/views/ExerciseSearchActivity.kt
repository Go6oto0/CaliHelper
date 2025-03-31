package com.georgiyordanov.calihelper.views

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.georgiyordanov.calihelper.data.models.Exercise
import com.georgiyordanov.calihelper.data.models.ExerciseName
import com.georgiyordanov.calihelper.data.models.SingleExerciseResponse
import com.georgiyordanov.calihelper.R
import com.georgiyordanov.calihelper.databinding.ActivityExerciseSearchBinding
import com.georgiyordanov.calihelper.network.RetrofitInstance
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ExerciseSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityExerciseSearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityExerciseSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup the hamburger button for the navigation drawer.
        binding.topBarInclude.hamburgerButton.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // Setup the navigation view listener.
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_exercises -> startActivity(Intent(this, ExerciseSearchActivity::class.java))
                R.id.nav_calorie_tracker -> startActivity(Intent(this, CalorieTrackerActivity::class.java))
                R.id.nav_workouts -> startActivity(Intent(this, WorkoutsActivity::class.java))
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

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

        // Handle selection.
        binding.searchBar.setOnItemClickListener { parent, _, position, _ ->
            val selected = parent.getItemAtPosition(position) as ExerciseName
            // Call the API using the seeded ID to fetch full exercise details.
            lifecycleScope.launch {
                try {
                    // getExerciseById now returns a SingleExerciseResponse.
                    val response = RetrofitInstance.api.getExerciseById(selected.id)
                    val exercise: Exercise = response.data
                    // Start ExerciseDetailActivity, passing the complete Exercise object.
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
