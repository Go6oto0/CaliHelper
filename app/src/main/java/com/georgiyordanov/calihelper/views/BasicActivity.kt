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
import com.georgiyordanov.calihelper.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class BasicActivity : AppCompatActivity() {
    protected val authViewModel: AuthViewModel by viewModels()
    protected lateinit var basicBinding: ActivityBasicBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        basicBinding = ActivityBasicBinding.inflate(layoutInflater)
        setContentView(basicBinding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowInsetsControllerCompat(window, basicBinding.root)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setupNavigation()
    }

    private fun setupNavigation() {
        // Hamburger always opens drawer
        basicBinding.topBarInclude.hamburgerButton.setOnClickListener {
            basicBinding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // Logout button only when Firebase user logged in
        if (authViewModel.isUserLoggedIn()) {
            basicBinding.topBarInclude.logoutButton.visibility = View.VISIBLE
        } else {
            basicBinding.topBarInclude.logoutButton.visibility = View.GONE
        }
        basicBinding.topBarInclude.logoutButton.setOnClickListener {
            authViewModel.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Configure nav-items based on the currently logged‐in app User
        authViewModel.fetchCurrentAppUser { user ->
            val menu = basicBinding.navigationView.menu

            if (user == null) {
                // not signed in → only show Login/Register
                menu.findItem(R.id.nav_login)?.isVisible            = true
                menu.findItem(R.id.nav_register)?.isVisible         = true
                menu.findItem(R.id.nav_meals)?.isVisible            = false
                menu.findItem(R.id.nav_calorie_tracker)?.isVisible  = false
                menu.findItem(R.id.nav_workouts)?.isVisible         = false
                menu.findItem(R.id.nav_suggest_workouts)?.isVisible = false
                menu.findItem(R.id.nav_profile)?.isVisible          = false
                menu.findItem(R.id.nav_registered_users)?.isVisible = false
            } else {
                // signed in → base items
                menu.findItem(R.id.nav_login)?.isVisible    = false
                menu.findItem(R.id.nav_register)?.isVisible = false
                menu.findItem(R.id.nav_profile)?.isVisible  = true

                // always show Meals
                menu.findItem(R.id.nav_meals)?.isVisible = true

                // rename & always show "Suggested Workouts"
                menu.findItem(R.id.nav_suggest_workouts)?.apply {
                    isVisible = true
                    title     = "Suggested Workouts"
                }

                // hide/show these based on profileSetup flag
                val setup = user.profileSetup
                menu.findItem(R.id.nav_calorie_tracker)?.isVisible = setup
                menu.findItem(R.id.nav_workouts)?.isVisible        = setup

                // admin-only item
                menu.findItem(R.id.nav_registered_users)
                    ?.isVisible = authViewModel.isUserAdmin()
            }
        }

        // Menu item click handling
        basicBinding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_exercises        -> startActivity(Intent(this, ExerciseSearchActivity::class.java))
                R.id.nav_calorie_tracker  -> startActivity(Intent(this, CalorieTrackerActivity::class.java))
                R.id.nav_meals            -> startActivity(Intent(this, MealsActivity::class.java))
                R.id.nav_workouts         -> startActivity(Intent(this, WorkoutsActivity::class.java))
                R.id.nav_suggest_workouts -> startActivity(Intent(this, WorkoutSuggestorActivity::class.java))
                R.id.nav_profile          -> startActivity(Intent(this, MainActivity::class.java))
                R.id.nav_login            -> startActivity(Intent(this, LoginActivity::class.java))
                R.id.nav_register         -> startActivity(Intent(this, RegisterActivity::class.java))
                R.id.nav_registered_users -> startActivity(Intent(this, RegisteredUsersActivity::class.java))
            }
            basicBinding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }
}
