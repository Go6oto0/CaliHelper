package com.georgiyordanov.calihelper.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.georgiyordanov.calihelper.data.models.ExerciseName
import com.georgiyordanov.calihelper.data.models.WorkoutPlan
import com.georgiyordanov.calihelper.databinding.ItemWorkoutBinding

class WorkoutAdapter(
    private var workouts: List<WorkoutPlan>,
    private var exerciseNames: List<ExerciseName> = emptyList(),
    private val onEdit: ((WorkoutPlan) -> Unit)? = null,
    private val onDelete: ((WorkoutPlan) -> Unit)? = null
) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    inner class WorkoutViewHolder(private val binding: ItemWorkoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(workout: WorkoutPlan) {
            binding.apply {
                tvWorkoutName.text = workout.name
                tvWorkoutDescription.text = workout.description

                // Resolve exercise IDs to names and format for display
                val exerciseDetails = workout.exercises.joinToString("\n") { exercise ->
                    val name = exerciseNames.find { it.id == exercise.exerciseId }?.toString() ?: "Unknown"
                    "$name – ${exercise.sets} sets, ${exercise.repetitions} reps"
                }

                tvWorkoutExercises.text = exerciseDetails

                btnEditWorkout.setOnClickListener { onEdit?.invoke(workout) }
                btnDeleteWorkout.setOnClickListener { onDelete?.invoke(workout) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemWorkoutBinding.inflate(inflater, parent, false)
        return WorkoutViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        holder.bind(workouts[position])
    }

    override fun getItemCount(): Int = workouts.size

    fun submitList(newWorkouts: List<WorkoutPlan>) {
        workouts = newWorkouts
        notifyDataSetChanged()
    }

    fun updateExerciseNames(newNames: List<ExerciseName>) {
        exerciseNames = newNames
        notifyDataSetChanged()
    }
}
