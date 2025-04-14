package com.georgiyordanov.calihelper.views

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.georgiyordanov.calihelper.R
import com.georgiyordanov.calihelper.data.models.User
import com.georgiyordanov.calihelper.databinding.ActivityRegisterBinding
import com.georgiyordanov.calihelper.ui.theme.viewmodels.AuthState
import com.georgiyordanov.calihelper.ui.theme.viewmodels.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class RegisterActivity : BasicActivity() {
    // Reuse the protected authViewModel from BasicActivity.
    // Instantiate a separate UserViewModel for registration details.
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var registerBinding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Inflate the registration layout.
        registerBinding = ActivityRegisterBinding.inflate(layoutInflater)
        // Insert the register view into the dynamic container within BasicActivity.
        basicBinding.contentFrame.removeAllViews()
        basicBinding.contentFrame.addView(registerBinding.root)
        setupClickListeners()
        setupObservers()
    }

    private fun setupClickListeners() {
        registerBinding.btnRegister.setOnClickListener {
            val email = registerBinding.etEmail.text.toString().trim()
            val password = registerBinding.etPassword.text.toString().trim()
            if (validateInput(email, password)) {
                // Use inherited authViewModel to sign up the user.
                authViewModel.signUp(email, password)
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showError("Invalid email")
                false
            }
            password.length < 6 -> {
                showError("Password must be at least 6 characters")
                false
            }
            else -> true
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.authState.collect { state ->
                    when (state) {
                        AuthState.Loading -> registerBinding.progressBar.visibility = View.VISIBLE
                        AuthState.Success -> handleSuccess()
                        is AuthState.Error -> showError(state.message)
                        else -> Unit
                    }
                }
            }
        }
        // Check if already logged in and then redirect to MainActivity.
        if (authViewModel.isUserLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun handleSuccess() {
        registerBinding.progressBar.visibility = View.GONE
        createUserProfile()
        Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showError(message: String?) {
        registerBinding.progressBar.visibility = View.GONE
        Toast.makeText(this, message ?: "Error occurred", Toast.LENGTH_SHORT).show()
    }

    private fun createUserProfile() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val user = User(
                uid = currentUser.uid
                // Add additional fields as necessary.
            )
            userViewModel.createUser(user)
        } else {
            showError("User not found")
        }
    }
}
