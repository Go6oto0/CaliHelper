package com.georgiyordanov.calihelper.views

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.georgiyordanov.calihelper.databinding.ActivityRegisteredUsersBinding
import com.georgiyordanov.calihelper.data.models.User
import com.georgiyordanov.calihelper.views.adapters.RegisteredUsersAdapter
import com.google.firebase.firestore.FirebaseFirestore

class RegisteredUsersActivity : BasicActivity() {

    private lateinit var binding: ActivityRegisteredUsersBinding
    private val db = FirebaseFirestore.getInstance()
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
        loadUsers()
    }

    private fun setupRecycler() {
        adapter = RegisteredUsersAdapter(emptyList()) { user ->
            deleteUser(user)
        }
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = adapter
    }

    private fun loadUsers() {
        db.collection("users")
            .get()
            .addOnSuccessListener { snap ->
                val users = snap.toObjects(User::class.java)
                adapter.updateData(users)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading users: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteUser(user: User) {
        db.collection("users")
            .document(user.uid)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Deleted ${user.userName ?: user.uid}", Toast.LENGTH_SHORT).show()
                loadUsers()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Delete failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
