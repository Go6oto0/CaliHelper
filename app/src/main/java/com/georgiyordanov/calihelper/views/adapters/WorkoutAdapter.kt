package com.georgiyordanov.calihelper.views.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.georgiyordanov.calihelper.data.models.ExerciseName
import com.georgiyordanov.calihelper.data.models.WorkoutPlan
import com.georgiyordanov.calihelper.databinding.ItemWorkoutBinding

class WorkoutAdapter(
    private var workouts: List<WorkoutPlan>,
    private var exerciseNames: List<ExerciseName> = emptyList(),
    // now both callbacks are nullable; if you don’t pass them, the buttons hide
    private val onEdit: ((WorkoutPlan) -> Unit)? = null,
    private val onDelete: ((WorkoutPlan) -> Unit)? = null
) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    inner class WorkoutViewHolder(private val binding: ItemWorkoutBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(workout: WorkoutPlan) = binding.run {
            tvWorkoutName.text = workout.name
            tvWorkoutDescription.text = workout.description

            // Resolve ID → name properly
            val exerciseDetails = workout.exercises.joinToString("\n") { ex ->
                val name = exerciseNames
                    .find { it.id == ex.exerciseId }
                    ?.name
                    ?: "Unknown"
                "$name – ${ex.sets} sets, ${ex.repetitions} reps"
            }
            tvWorkoutExercises.text = exerciseDetails

            // Show/hide and wire up the buttons
            btnEditWorkout.visibility   = if (onEdit   != null) View.VISIBLE else View.GONE
            btnDeleteWorkout.visibility = if (onDelete != null) View.VISIBLE else View.GONE

            onEdit?.let { editCb ->
                btnEditWorkout.setOnClickListener { editCb(workout) }
            }
            onDelete?.let { delCb ->
                btnDeleteWorkout.setOnClickListener { delCb(workout) }
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
