package com.georgiyordanov.calihelper.views.adapters

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.georgiyordanov.calihelper.R
import com.georgiyordanov.calihelper.data.models.User

class RegisteredUsersAdapter(
    private var users: List<User>,
    private val onDelete: (User) -> Unit
) : RecyclerView.Adapter<RegisteredUsersAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvName: TextView = view.findViewById(R.id.tvUserName)
        private val tvEmail: TextView = view.findViewById(R.id.tvEmail)
        private val tvCounts: TextView = view.findViewById(R.id.tvCounts)
        private val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteUser)

        fun bind(user: User) {
            tvName.text = user.userName ?: "(no name)"
            tvEmail.text = user.email ?: "(no email)"
            val workouts = user.workoutPlanIds.size
            val logs = user.calorieLogIds.size
            tvCounts.text = "Workouts: $workouts   Logs: $logs"

            btnDelete.setOnClickListener {
                AlertDialog.Builder(itemView.context)
                    .setTitle("Delete user?")
                    .setMessage("Are you sure you want to delete ${user.userName ?: user.uid}?")
                    .setPositiveButton("Yes") { _, _ -> onDelete(user) }
                    .setNegativeButton("No", null)
                    .show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_registered_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    fun updateData(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }
}
