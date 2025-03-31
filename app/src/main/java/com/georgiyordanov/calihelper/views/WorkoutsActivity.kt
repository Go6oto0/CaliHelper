package com.georgiyordanov.calihelper.views

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.georgiyordanov.calihelper.R
import com.georgiyordanov.calihelper.databinding.ActivityWorkoutsBinding

class WorkoutsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWorkoutsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityWorkoutsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        /*setContentView(R.layout.activity_workouts_repository)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/
        binding.topBarInclude.hamburgerButton.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_exercises -> {
                    startActivity(Intent(this, ExerciseSearchActivity::class.java))
                }
                R.id.nav_calorie_tracker -> {
                    startActivity(Intent(this, CalorieTrackerActivity::class.java))
                }
                R.id.nav_workouts -> {
                    startActivity(Intent(this, WorkoutsActivity::class.java))
                }
            }
            // Close the drawer after an item is clicked.
            binding.drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
            true
        }
    }
}