package com.georgiyordanov.calihelper.views

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.georgiyordanov.calihelper.data.models.Meal
import com.georgiyordanov.calihelper.databinding.ActivityMealDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MealDetailActivity : BasicActivity() {

    private lateinit var binding: ActivityMealDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate your detail layout into the BasicActivity container
        binding = ActivityMealDetailBinding.inflate(layoutInflater)
        basicBinding.contentFrame.addView(binding.root)

        // Edge‑to‑edge insets
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sysBars.left, sysBars.top, sysBars.right, sysBars.bottom)
            insets
        }

        // Retrieve the passed Meal (Serializable)
        val meal = intent.getSerializableExtra("meal") as? Meal
            ?: run { finish(); return }

        // Populate the UI
        binding.tvName.text = meal.name
        binding.tvCalories.text = meal.calories?.let { "$it kcal" } ?: ""
        binding.tvDescription.text = meal.description ?: ""
        binding.tvIngredients.text = meal.ingredients.joinToString("\n") { "• $it" }
        binding.tvInstructions.text = meal.instructions.joinToString("\n\n") { it }
    }
}
