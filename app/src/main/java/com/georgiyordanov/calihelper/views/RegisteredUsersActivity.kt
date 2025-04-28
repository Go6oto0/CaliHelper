package com.georgiyordanov.calihelper.views

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.georgiyordanov.calihelper.databinding.ActivityRegisteredUsersBinding
import com.georgiyordanov.calihelper.data.models.User
import com.georgiyordanov.calihelper.viewmodels.UserState
import com.georgiyordanov.calihelper.viewmodels.UserViewModel
import com.georgiyordanov.calihelper.views.adapters.RegisteredUsersAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RegisteredUsersActivity : BasicActivity() {

    private lateinit var binding: ActivityRegisteredUsersBinding
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var adapter: RegisteredUsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisteredUsersBinding.inflate(layoutInflater)
        basicBinding.contentFrame.addView(binding.root)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        setupRecycler()
        observeUserState()

        // initial load
        loadUsers()
    }

    private fun setupRecycler() {
        adapter = RegisteredUsersAdapter(emptyList()) { user ->
            confirmAndDelete(user)
        }
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = adapter
    }

    private fun observeUserState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.userState.collectLatest { state ->
                    when (state) {
                        is UserState.UsersList -> {
                            adapter.updateData(state.users.orEmpty())
                        }
                        is UserState.Error -> {
                            Toast.makeText(
                                this@RegisteredUsersActivity,
                                "Error: ${state.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> {
                            // Idle or Loading or Success: no-op
                        }
                    }
                }
            }
        }
    }

    private fun loadUsers() {
        userViewModel.readAllUsers()
    }

    private fun confirmAndDelete(user: User) {
        AlertDialog.Builder(this)
            .setTitle("Delete “${user.userName ?: user.uid}”?")
            .setMessage("This will permanently delete the user and all their data.")
            .setPositiveButton("Delete") { dialog, _ ->
                dialog.dismiss()
                userViewModel.cascadeDeleteUserData(user.uid) { error ->
                    runOnUiThread {
                        if (error == null) {
                            Toast.makeText(
                                this,
                                "Deleted ${user.userName ?: user.uid}",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this,
                                "Delete failed: ${error.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        // refresh the users list afterward
                        loadUsers()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
