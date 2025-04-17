package com.georgiyordanov.calihelper.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

import androidx.core.view.WindowInsetsControllerCompat
import com.georgiyordanov.calihelper.R
import com.georgiyordanov.calihelper.databinding.ActivityBasicBinding
import com.georgiyordanov.calihelper.ui.theme.viewmodels.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

open class BasicActivity : AppCompatActivity() {
    // Make the authViewModel protected so child activities (like Login and Register) can reuse it.
    protected val authViewModel: AuthViewModel by viewModels()
    protected lateinit var basicBinding: ActivityBasicBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        basicBinding = ActivityBasicBinding.inflate(layoutInflater)
        setContentView(basicBinding.root)

        // Enable fullscreen mode
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowInsetsControllerCompat(window, basicBinding.root)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setupNavigation()
    }

    // Navigation method
    private fun setupNavigation() {
        // Hamburger button: always available to open the drawer.
        basicBinding.topBarInclude.hamburgerButton.setOnClickListener {
            basicBinding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // Conditional logout button in the top bar.
        // It appears only when a user is logged in.
        if (authViewModel.isUserLoggedIn()) {
            basicBinding.topBarInclude.logoutButton.visibility = View.VISIBLE
        } else {
            basicBinding.topBarInclude.logoutButton.visibility = View.GONE
        }
        basicBinding.topBarInclude.logoutButton.setOnClickListener {
            authViewModel.logout()
            // Redirect to LoginActivity or however you wish to handle logout.
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Example: Hide navigation items that are for registered users if the user is not logged in.
        if (!authViewModel.isUserLoggedIn()) {
            basicBinding.navigationView.menu.findItem(R.id.nav_login)?.isVisible = true
            basicBinding.navigationView.menu.findItem(R.id.nav_register)?.isVisible = true
            basicBinding.navigationView.menu.findItem(R.id.nav_calorie_tracker).isVisible = false
            basicBinding.navigationView.menu.findItem(R.id.nav_meals).isVisible = false
            basicBinding.navigationView.menu.findItem(R.id.nav_profile).isVisible = false
            basicBinding.navigationView.menu.findItem(R.id.nav_workouts).isVisible = false
            // Hide admin-only navigation item as well.
            basicBinding.navigationView.menu.findItem(R.id.nav_registered_users)?.isVisible = false
        } else {
            // If the user is logged in, they see the normal navigation items.
            basicBinding.navigationView.menu.findItem(R.id.nav_login)?.isVisible = false
            basicBinding.navigationView.menu.findItem(R.id.nav_register)?.isVisible = false
            basicBinding.navigationView.menu.findItem(R.id.nav_calorie_tracker).isVisible = true
            basicBinding.navigationView.menu.findItem(R.id.nav_profile).isVisible = true
            basicBinding.navigationView.menu.findItem(R.id.nav_meals).isVisible = true
            basicBinding.navigationView.menu.findItem(R.id.nav_workouts).isVisible = true

            // Show the admin navigation item only if the user is an admin.
            if (authViewModel.isUserAdmin()) {
                basicBinding.navigationView.menu.findItem(R.id.nav_registered_users)?.isVisible = true
            } else {
                basicBinding.navigationView.menu.findItem(R.id.nav_registered_users)?.isVisible = false
            }
        }

        // Set up navigation menu item click listener.
        basicBinding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_exercises -> startActivity(Intent(this, ExerciseSearchActivity::class.java))
                R.id.nav_calorie_tracker -> startActivity(Intent(this, CalorieTrackerActivity::class.java))
                R.id.nav_meals -> startActivity(Intent(this, MealsActivity::class.java))
                R.id.nav_workouts -> startActivity(Intent(this, WorkoutsActivity::class.java))

                // ← New case to launch the Suggestor:
                R.id.nav_suggest_workouts -> startActivity(
                    Intent(this, WorkoutSuggestorActivity::class.java)
                )

                R.id.nav_profile -> startActivity(Intent(this, MainActivity::class.java))
                R.id.nav_login -> startActivity(Intent(this, LoginActivity::class.java))
                R.id.nav_register -> startActivity(Intent(this, RegisterActivity::class.java))
                R.id.nav_registered_users -> {
                    startActivity(Intent(this, RegisteredUsersActivity::class.java))
                }
            }
            basicBinding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }
}
