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
        // Inflate the login layout
        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        // Insert it into the content frame defined in BasicActivity
        basicBinding.contentFrame.removeAllViews() // clear any existing views
        basicBinding.contentFrame.addView(loginBinding.root)

        setupObservers()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        loginBinding.btnLogin.setOnClickListener {
            val email = loginBinding.etEmail.text.toString().trim()
            val password = loginBinding.etPassword.text.toString().trim()
            // Use the inherited authViewModel for signing in.
            authViewModel.signIn(email, password)
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.authState.collect { state ->
                    when (state) {
                        AuthState.Loading -> loginBinding.progressBar.visibility = View.VISIBLE
                        AuthState.Success -> handleSuccess()
                        is AuthState.Error -> showError(state.message)
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun handleSuccess() {
        loginBinding.progressBar.visibility = View.GONE
        Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showError(message: String?) {
        loginBinding.progressBar.visibility = View.GONE
        Toast.makeText(this, message ?: "Error occurred", Toast.LENGTH_SHORT).show()
    }
}
