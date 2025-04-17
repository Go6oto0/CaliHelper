package com.georgiyordanov.calihelper.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.georgiyordanov.calihelper.databinding.ActivityLoginBinding
import com.georgiyordanov.calihelper.ui.theme.viewmodels.AuthState
import kotlinx.coroutines.launch

class LoginActivity : BasicActivity() {
    private lateinit var loginBinding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate and swap in the login layout
        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        basicBinding.contentFrame.removeAllViews()
        basicBinding.contentFrame.addView(loginBinding.root)

        setupObservers()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        loginBinding.btnLogin.setOnClickListener {
            val email = loginBinding.etEmail.text.toString().trim()
            val password = loginBinding.etPassword.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            authViewModel.signIn(email, password)
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            // Only collect when at least STARTED
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.authState.collect { state ->
                    when (state) {
                        AuthState.Loading -> {
                            loginBinding.progressBar.visibility = View.VISIBLE
                            setInputsEnabled(false)
                        }
                        AuthState.Success -> {
                            loginBinding.progressBar.visibility = View.GONE
                            setInputsEnabled(true)
                            handleSuccess()
                        }
                        is AuthState.Error -> {
                            loginBinding.progressBar.visibility = View.GONE
                            setInputsEnabled(true)
                            showError(state.message)
                        }
                        else -> {
                            // Idle: no-op
                        }
                    }
                }
            }
        }
    }

    private fun setInputsEnabled(enabled: Boolean) {
        loginBinding.etEmail.isEnabled = enabled
        loginBinding.etPassword.isEnabled = enabled
        loginBinding.btnLogin.isEnabled = enabled
    }

    private fun handleSuccess() {
        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showError(message: String?) {
        Toast.makeText(this, message ?: "Login failed", Toast.LENGTH_LONG).show()
    }
}
