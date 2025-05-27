package com.georgiyordanov.calihelper.views

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.enableEdgeToEdge
import com.georgiyordanov.calihelper.R
import com.georgiyordanov.calihelper.data.models.WorkoutExercise
import com.georgiyordanov.calihelper.data.models.WorkoutPlan
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SeedSuggestedWorkoutsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_seed_suggested_workouts)
        Log.d("enteredseed", "enteredseed")
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        val db = FirebaseFirestore.getInstance()

        // Pull workouts (4 exercises each)
        val pull1 = WorkoutPlan(
            id = "pull_1", userId = "",
            name = "Pull-Up Pyramid",
            description = "1–5 pull‑ups up and down pyramid",
            exercises = listOf(
                WorkoutExercise("pull_1_ex1", "pull_1", "pull up (neutral grip)", 5, 1),
                WorkoutExercise("pull_1_ex2", "pull_1", "archer pull up", 4, 2),
                WorkoutExercise("pull_1_ex3", "pull_1", "suspended row", 8, 3),
                WorkoutExercise("pull_1_ex4", "pull_1", "bodyweight squatting row", 10, 3)
            )
        )
        val pull2 = WorkoutPlan(
            id = "pull_2", userId = "",
            name = "Inverted Row Progression",
            description = "4× max inverted rows at varying angles",
            exercises = listOf(
                WorkoutExercise("pull_2_ex1", "pull_2", "bodyweight squatting row", 12, 3),
                WorkoutExercise("pull_2_ex2", "pull_2", "suspended row", 10, 3),
                WorkoutExercise("pull_2_ex3", "pull_2", "band fixed back close grip pulldown", 12, 3),
                WorkoutExercise("pull_2_ex4", "pull_2", "alternate lateral pulldown", 10, 3)
            )
        )

        // Push workouts (4 exercises each)
        val push1 = WorkoutPlan(
            id = "push_1", userId = "",
            name = "Diamond Push‑Up Ladder",
            description = "3–7 diamond push‑ups and back down",
            exercises = listOf(
                WorkoutExercise("push_1_ex1", "push_1", "incline close-grip push-up", 8, 3),
                WorkoutExercise("push_1_ex2", "push_1", "push-up (bosu ball)", 6, 3),
                WorkoutExercise("push_1_ex3", "push_1", "push-up inside leg kick", 8, 3),
                WorkoutExercise("push_1_ex4", "push_1", "push-up on lower arms", 10, 3)
            )
        )
        val push2 = WorkoutPlan(
            id = "push_2", userId = "",
            name = "Decline Push‑Up Set",
            description = "5×10 decline push‑ups, feet elevated",
            exercises = listOf(
                WorkoutExercise("push_2_ex1", "push_2", "superman push-up", 6, 3),
                WorkoutExercise("push_2_ex2", "push_2", "incline close-grip push-up", 8, 3),
                WorkoutExercise("push_2_ex3", "push_2", "push-up inside leg kick", 8, 3),
                WorkoutExercise("push_2_ex4", "push_2", "push-up on lower arms", 10, 3)
            )
        )

        // Legs workouts (4 exercises each)
        val legs1 = WorkoutPlan(
            id = "legs_1", userId = "",
            name = "Pistol Squat Practice",
            description = "3×5 assisted pistol squats per leg",
            exercises = listOf(
                WorkoutExercise("legs_1_ex1", "legs_1", "kettlebell pistol squat", 5, 3),
                WorkoutExercise("legs_1_ex2", "legs_1", "one leg floor calf raise", 12, 3),
                WorkoutExercise("legs_1_ex3", "legs_1", "potty squat", 15, 3),
                WorkoutExercise("legs_1_ex4", "legs_1", "semi squat jump (male)", 10, 3)
            )
        )
        val legs2 = WorkoutPlan(
            id = "legs_2", userId = "",
            name = "Jump Squat Circuit",
            description = "4 rounds of 15 jump squats",
            exercises = listOf(
                WorkoutExercise("legs_2_ex1", "legs_2", "jack jump (male)", 15, 4),
                WorkoutExercise("legs_2_ex2", "legs_2", "semi squat jump (male)", 12, 4),
                WorkoutExercise("legs_2_ex3", "legs_2", "weighted stretch lunge", 10, 3),
                WorkoutExercise("legs_2_ex4", "legs_2", "weighted lunge with swing", 10, 3)
            )
        )

        // Core workouts (4 exercises each)
        val core1 = WorkoutPlan(
            id = "core_1", userId = "",
            name = "Hanging Knee Raise",
            description = "3×12 hanging knee raises",
            exercises = listOf(
                WorkoutExercise("core_1_ex1", "core_1", "assisted hanging knee raise", 12, 3),
                WorkoutExercise("core_1_ex2", "core_1", "hanging straight leg raise", 10, 3),
                WorkoutExercise("core_1_ex3", "core_1", "band kneeling twisting crunch", 15, 3),
                WorkoutExercise("core_1_ex4", "core_1", "3/4 sit-up", 20, 3)
            )
        )
        val core2 = WorkoutPlan(
            id = "core_2", userId = "",
            name = "Plank Twist Combo",
            description = "3 rounds: plank + twist",
            exercises = listOf(
                WorkoutExercise("core_2_ex1", "core_2", "prone twist on stability ball", 20, 3),
                WorkoutExercise("core_2_ex2", "core_2", "janda sit-up", 15, 3),
                WorkoutExercise("core_2_ex3", "core_2", "band kneeling twisting crunch", 15, 3),
                WorkoutExercise("core_2_ex4", "core_2", "3/4 sit-up", 20, 3)
            )
        )

        // Full-body workouts (2 pull, 2 push, 2 legs, 2 core)
        val fullBody1 = WorkoutPlan(
            id = "fullbody_1", userId = "",
            name = "Burpee EMOM",
            description = "Every minute: 10 burpees",
            exercises = listOf(
                WorkoutExercise("fb1_ex1", "fullbody_1", "pull up (neutral grip)", 8, 3),
                WorkoutExercise("fb1_ex2", "fullbody_1", "archer pull up", 6, 3),
                WorkoutExercise("fb1_ex3", "fullbody_1", "incline close-grip push-up", 10, 3),
                WorkoutExercise("fb1_ex4", "fullbody_1", "push-up inside leg kick", 8, 3),
                WorkoutExercise("fb1_ex5", "fullbody_1", "kettlebell pistol squat", 5, 3),
                WorkoutExercise("fb1_ex6", "fullbody_1", "semi squat jump (male)", 10, 3),
                WorkoutExercise("fb1_ex7", "fullbody_1", "assisted hanging knee raise", 12, 3),
                WorkoutExercise("fb1_ex8", "fullbody_1", "band kneeling twisting crunch", 15, 3)
            )
        )
        val fullBody2 = WorkoutPlan(
            id = "fullbody_2", userId = "",
            name = "Bear Crawl Circuit",
            description = "5 rounds: 20 m bear crawl",
            exercises = listOf(
                WorkoutExercise("fb2_ex1", "fullbody_2", "pull up (neutral grip)", 8, 3),
                WorkoutExercise("fb2_ex2", "fullbody_2", "archer pull up", 6, 3),
                WorkoutExercise("fb2_ex3", "fullbody_2", "incline close-grip push-up", 10, 3),
                WorkoutExercise("fb2_ex4", "fullbody_2", "push-up inside leg kick", 8, 3),
                WorkoutExercise("fb2_ex5", "fullbody_2", "kettlebell pistol squat", 5, 3),
                WorkoutExercise("fb2_ex6", "fullbody_2", "semi squat jump (male)", 10, 3),
                WorkoutExercise("fb2_ex7", "fullbody_2", "assisted hanging knee raise", 12, 3),
                WorkoutExercise("fb2_ex8", "fullbody_2", "band kneeling twisting crunch", 15, 3)
            )
        )

        // Helper to seed
        fun seed(plans: List<WorkoutPlan>, type: String) {
            plans.forEach { plan -> Log.d("enteredseed", plan.name)
                val data = plan.toMap().toMutableMap().apply { put("type", type) }
                db.collection("workoutPlanSuggestions").document(plan.id).set(data)
            }
        }

        seed(listOf(pull1, pull2), "pull")
        seed(listOf(push1, push2), "push")
        seed(listOf(legs1, legs2), "legs")
        seed(listOf(core1, core2), "core")
        seed(listOf(fullBody1, fullBody2), "fullbody")
    }
}
