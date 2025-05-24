package com.georgiyordanov.calihelper.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.georgiyordanov.calihelper.data.models.WorkoutExercise
import com.georgiyordanov.calihelper.databinding.ItemExerciseBinding

class WorkoutExerciseAdapter(
    private val items: MutableList<WorkoutExercise>,
    private val onRemoveClick: (position: Int) -> Unit,
    private val onItemClick: ((position: Int) -> Unit)? = null,
    // Lambda to look up the exercise name given an id.
    private val nameLookup: (String) -> String
) : RecyclerView.Adapter<WorkoutExerciseAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemExerciseBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.btnRemove.setOnClickListener {
                onRemoveClick(adapterPosition)
            }
            binding.cardExercise.setOnClickListener {
                onItemClick?.invoke(adapterPosition)
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExerciseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workoutExercise = items[position]
        val exerciseName = nameLookup(workoutExercise.exerciseId)
        holder.binding.tvExerciseDetails.text =
            "$exerciseName - ${workoutExercise.sets} sets, ${workoutExercise.repetitions} reps"
        Log.d("WorkoutExerciseAdapter", "Binding position $position, list size: ${items.size}")
    }

    override fun getItemCount(): Int {
        Log.d("WorkoutExerciseAdapter", "Current item count: ${items.size}")
        return items.size
    }
}
