package com.georgiyordanov.calihelper.views

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.georgiyordanov.calihelper.R
import com.georgiyordanov.calihelper.data.models.User
import com.georgiyordanov.calihelper.databinding.ActivityRegisterBinding
import com.georgiyordanov.calihelper.ui.theme.viewmodels.AuthState
import com.georgiyordanov.calihelper.ui.theme.viewmodels.AuthViewModel
import com.georgiyordanov.calihelper.ui.theme.viewmodels.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var binding: ActivityRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/
        setupObservers()
        setupClickListeners()
    }
    // Add in onCreate after setContentView
    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            if (validateInput(email, password)) {
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
                        AuthState.Loading -> binding.progressBar.visibility = View.VISIBLE
                        AuthState.Success -> handleSuccess()
                        is AuthState.Error -> showError(state.message)
                        else -> Unit
                    }
                }
            }
        }
        if (authViewModel.isUserLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Optional: Close RegisterActivity
        }
    }

    private fun handleSuccess() {
        binding.progressBar.visibility = View.GONE
        createUserProfile()
        startActivity(Intent(this, MainActivity::class.java))
        Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show()
    }

    private fun showError(message: String?) {
        binding.progressBar.visibility = View.GONE
        Toast.makeText(this, message ?: "Error occurred", Toast.LENGTH_SHORT).show()
    }

    private fun createUserProfile() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val user = User(
                uid = currentUser.uid
            )
            // Call your view model's createUser method
            userViewModel.createUser(user)
        } else {
            showError("User not found")
        }
    }
}