package com.georgiyordanov.calihelper.views

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.georgiyordanov.calihelper.R
import com.georgiyordanov.calihelper.viewmodels.UserState
import com.georgiyordanov.calihelper.viewmodels.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileSetupActivity : BasicActivity() {

    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout dedicated for profile setup
        setContentView(R.layout.activity_profile_setup)

        // Get the current user's UID; if not logged in, send them to the login screen.
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Reference views from the layout.
        val etUserName = findViewById<EditText>(R.id.etUserName)
        val etWeight = findViewById<EditText>(R.id.etWeight)
        val etHeight = findViewById<EditText>(R.id.etHeight)
        val etAge = findViewById<EditText>(R.id.etAge)
        val etGoal = findViewById<EditText>(R.id.etGoal)
        val btnSave = findViewById<Button>(R.id.btnSave)

        // Observe the userViewModel state.
        lifecycleScope.launch {
            userViewModel.userState.collectLatest { state ->
                when (state) {
                    is UserState.Success -> {
                        Toast.makeText(this@ProfileSetupActivity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@ProfileSetupActivity, MainActivity::class.java))
                        finish()
                    }
                    is UserState.Error -> {
                        Toast.makeText(this@ProfileSetupActivity, "Error updating profile: ${state.message}", Toast.LENGTH_LONG).show()
                    }
                    else -> { /* Optional: Handle Idle or Loading states */ }
                }
            }
        }

        // Set onClickListener for the save button.
        btnSave.setOnClickListener {
            // Retrieve values entered by the user.
            val userName = etUserName.text.toString().trim()
            val weightInput = etWeight.text.toString().trim()
            val heightInput = etHeight.text.toString().trim()
            val ageInput = etAge.text.toString().trim()
            val goal = etGoal.text.toString().trim()

            // Validate none of the required fields are empty.
            if (userName.isEmpty() || weightInput.isEmpty() || heightInput.isEmpty() ||
                ageInput.isEmpty() || goal.isEmpty()
            ) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Convert string inputs to the appropriate number types.
            val weight = weightInput.toFloatOrNull()
            val height = heightInput.toFloatOrNull()
            val age = ageInput.toIntOrNull()

            if (weight == null || height == null || age == null) {
                Toast.makeText(this, "Invalid numerical values", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create a map containing the updates.
            val updates = mapOf(
                "userName" to userName,
                "weight" to weight,
                "height" to height,
                "age" to age,
                "goal" to goal,
                "profileSetup" to true
            )

            // Use the UserViewModel to update the user's data.
            userViewModel.updateUser(uid, updates)
            // Notice we no longer immediately show the toast or call finish()
            // Instead we wait until the update operation completes and the StateFlow is updated.
        }
    }
}
