package com.georgiyordanov.calihelper.views

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.georgiyordanov.calihelper.R
import com.georgiyordanov.calihelper.databinding.ActivityBasicBinding

open class BasicActivity : AppCompatActivity() {

    protected lateinit var basicBinding: ActivityBasicBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        basicBinding = ActivityBasicBinding.inflate(layoutInflater)
        setContentView(basicBinding.root)

        // Enable fullscreen mode using the WindowInsets API.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowInsetsControllerCompat(window, basicBinding.root)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setupNavigation()
    }
    //Navigation method
    private fun setupNavigation() {
        basicBinding.topBarInclude.hamburgerButton.setOnClickListener {
            basicBinding.drawerLayout.openDrawer(GravityCompat.START)
        }

        basicBinding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_exercises -> startActivity(Intent(this, ExerciseSearchActivity::class.java))
                R.id.nav_calorie_tracker -> startActivity(Intent(this, CalorieTrackerActivity::class.java))
                R.id.nav_workouts -> startActivity(Intent(this, WorkoutsActivity::class.java))
            }
            basicBinding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }
}
