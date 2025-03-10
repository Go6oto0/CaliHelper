package com.georgiyordanov.calihelper.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.georgiyordanov.calihelper.databinding.ActivityLoginBinding
import com.georgiyordanov.calihelper.ui.theme.viewmodels.AuthState
import com.georgiyordanov.calihelper.ui.theme.viewmodels.AuthViewModel
import kotlinx.coroutines.launch


class LoginActivity : AppCompatActivity() {
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var binding: ActivityLoginBinding
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            binding = ActivityLoginBinding.inflate(layoutInflater)
            setContentView(binding.root)
        /*setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/
            setupObservers()
            setupClickListeners()
    }
    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewModel.signIn(email, password)
        }

    }
    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collect { state ->
                    when (state) {
                        AuthState.Loading -> binding.progressBar.visibility = View.VISIBLE
                        AuthState.Success -> handleSuccess()
                        is AuthState.Error -> showError(state.message)
                        else -> Unit
                    }
                }
            }
        }
    }
    private fun handleSuccess() {
        binding.progressBar.visibility = View.GONE
        startActivity(Intent(this, MainActivity::class.java))
        Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show()
    }

    private fun showError(message: String?) {
        binding.progressBar.visibility = View.GONE
        Toast.makeText(this, message ?: "Error occurred", Toast.LENGTH_SHORT).show()
    }


}