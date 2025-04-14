package com.georgiyordanov.calihelper.views

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.georgiyordanov.calihelper.data.models.CommonFood
import com.georgiyordanov.calihelper.data.models.FoodDetail
import com.georgiyordanov.calihelper.databinding.ActivityFoodDetailBinding
import com.georgiyordanov.calihelper.viewmodels.CalorieTrackerViewModel
import com.georgiyordanov.calihelper.viewmodels.FoodDetailViewModel
import kotlinx.coroutines.launch

class FoodDetailActivity : BasicActivity() {

    private lateinit var binding: ActivityFoodDetailBinding

    // Use the FoodDetailViewModel
    private val viewModel: FoodDetailViewModel by viewModels()
    private val calorieTrackerViewModel: CalorieTrackerViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodDetailBinding.inflate(layoutInflater)
        basicBinding.contentFrame.addView(binding.root)

        // Retrieve the passed food item (ensure it is Serializable)
        val food = intent.getSerializableExtra("selectedFood") as? CommonFood
        if (food == null) {
            Toast.makeText(this, "Food not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val logDocId = intent.getStringExtra("logDocId")
        if (logDocId == null) {
            Toast.makeText(this, "Calorie log not available.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        // Display basic details from the food object.
        displayFoodDetails(food)

        // Set default custom serving to base value (assume 1 serving)
        binding.etCustomServing.setText(food.serving_qty.toString())

        // Observe nutrient details LiveData to update UI when available.
        viewModel.nutrientDetails.observe(this) { nutrientDetails ->
            if (nutrientDetails != null) {
                updateNutrientUI(nutrientDetails)
            } else {
                Log.e("FoodDetail", "No nutrient details returned.")
            }
        }

        // Automatically trigger calculation (post to ensure view is ready)
        binding.btnCalculate.post {
            viewModel.recalculateNutrients(food, binding.etCustomServing.text.toString().toDoubleOrNull() ?: food.serving_qty)
        }

        // Setup Calculate button listener for manual recalculation.
        binding.btnCalculate.setOnClickListener {
            recalculateNutrients(food)
        }
        binding.btnAddFoodToLog.setOnClickListener {
            val nutrientDetails = viewModel.nutrientDetails.value
            if (nutrientDetails != null) {
                val foodItem = viewModel.createFoodItem(food, nutrientDetails)
                lifecycleScope.launch {
                    calorieTrackerViewModel.updateCalorieLogWithFood(foodItem, logDocId)
                    Toast.makeText(this@FoodDetailActivity, "Food added to your log.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                Toast.makeText(this, "Nutrient details not available.", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun displayFoodDetails(food: CommonFood) {
        binding.tvFoodNameDetail.text = food.food_name
        binding.tvServingInfoDetail.text = "Serving: ${food.serving_qty} ${food.serving_unit}"
        // Optionally load the thumbnail image, e.g. with Glide:
        // Glide.with(this).load(food.photo.thumb).into(binding.ivFoodPhoto)
    }

    private fun recalculateNutrients(food: CommonFood) {
        Log.d("FoodDetail", "Calculate button pressed")
        val customServing = binding.etCustomServing.text.toString().toDoubleOrNull()
        if (customServing == null || customServing <= 0) {
            Toast.makeText(this, "Enter a valid serving size", Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.recalculateNutrients(food, customServing)
    }

    private fun updateNutrientUI(detail: FoodDetail) {
        Log.d("FoodDetail", "Updating nutrient UI")
        binding.tvTotalCalories.text = "Total Calories: %.2f".format(detail.nf_calories)
        binding.tvTotalCarbs.text = "Total Carbohydrates: %.2f".format(detail.nf_total_carbohydrate)
        binding.tvTotalFats.text = "Total Fats: %.2f".format(detail.nf_total_fat)
        binding.tvTotalProtein.text = "Total Protein: %.2f".format(detail.nf_protein)
        binding.tvServingInfoDetail.text = "Serving: ${detail.serving_qty} ${detail.serving_unit} (${detail.serving_weight_grams} grams)"
    }
}
