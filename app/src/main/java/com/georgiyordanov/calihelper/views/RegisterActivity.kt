package com.georgiyordanov.calihelper.views

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.georgiyordanov.calihelper.databinding.ActivityMainBinding
import com.georgiyordanov.calihelper.databinding.ActivityRegisterBinding
import com.georgiyordanov.calihelper.viewmodels.AuthState
import kotlinx.coroutines.launch

class RegisterActivity : BasicActivity() {
    // Inherited from BasicActivity:
    // protected val authViewModel: AuthViewModel by viewModels()

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        basicBinding.contentFrame.removeAllViews()
        basicBinding.contentFrame.addView(binding.root)

        setupClickListeners()
        setupObservers()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showError("Invalid email")
                return@setOnClickListener
            }
            if (password.length < 6) {
                showError("Password must be at least 6 characters")
                return@setOnClickListener
            }

            // Kick off sign-up in the shared AuthViewModel
            authViewModel.signUp(email, password)
        }
        binding.tvLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.authState.collect { state ->
                    when (state) {
                        AuthState.Loading -> binding.progressBar.visibility = View.VISIBLE
                        AuthState.Success -> onSignUpSuccess()
                        is AuthState.Error -> showError(state.message)
                        else -> Unit
                    }
                }
            }
        }

        // If the user somehow is already logged in, skip straight to Main:
        if (authViewModel.isUserLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun onSignUpSuccess() {
        binding.progressBar.visibility = View.GONE
        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showError(message: String?) {
        binding.progressBar.visibility = View.GONE
        Toast.makeText(this, message ?: "Error occurred", Toast.LENGTH_LONG).show()
    }
}
