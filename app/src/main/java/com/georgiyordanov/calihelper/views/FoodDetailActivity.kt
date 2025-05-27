// FoodDetailActivity.kt
package com.georgiyordanov.calihelper.views

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.georgiyordanov.calihelper.data.models.CommonFood
import com.georgiyordanov.calihelper.data.models.FoodDetail
import com.georgiyordanov.calihelper.data.models.FoodItem
import com.georgiyordanov.calihelper.data.repository.CalorieLogRepository
import com.georgiyordanov.calihelper.databinding.ActivityFoodDetailBinding
import com.georgiyordanov.calihelper.viewmodels.FoodDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.O)
class FoodDetailActivity : BasicActivity() {

    @Inject
    lateinit var calorieLogRepo: CalorieLogRepository

    private lateinit var binding: ActivityFoodDetailBinding
    private val viewModel: FoodDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodDetailBinding.inflate(layoutInflater)
        basicBinding.contentFrame.addView(binding.root)

        // 1) Retrieve the passed CommonFood
        val food = intent.getSerializableExtra("selectedFood") as? CommonFood
        if (food == null) {
            Toast.makeText(this, "Food not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 2) Retrieve the current log document ID
        val logDocId = intent.getStringExtra("logDocId")
        if (logDocId.isNullOrBlank()) {
            Toast.makeText(this, "Calorie log not available.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 3) Display basic info and load the image
        displayFoodDetails(food)

        // 4) Set default serving quantity
        binding.etCustomServing.setText(food.serving_qty.toString())

        // 5) Observe nutrient details LiveData
        viewModel.nutrientDetails.observe(this) { detail ->
            if (detail != null) {
                updateNutrientUI(detail)
            } else {
                Log.e("FoodDetailActivity", "Nutrient details null")
            }
        }

        // 6) Trigger initial calculation once layout is ready
        binding.btnCalculate.post {
            recalculateNutrients(food)
        }

        // 7) Calculate button listener
        binding.btnCalculate.setOnClickListener {
            recalculateNutrients(food)
        }

        // 8) Add-to-log button listener – directly using repository
        binding.btnAddFoodToLog.setOnClickListener {
            val detail = viewModel.nutrientDetails.value
            if (detail != null) {
                val foodItem = viewModel.createFoodItem(food, detail)
                // Write directly to Firestore via repository
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        calorieLogRepo.addFoodItem(logDocId, foodItem)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@FoodDetailActivity,
                                "Added to your log",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@FoodDetailActivity,
                                "Failed to add to log",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    "Nutrient details not available",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun displayFoodDetails(food: CommonFood) {
        binding.tvFoodNameDetail.text = food.food_name
        binding.tvServingInfoDetail.text =
            "Serving: ${food.serving_qty} ${food.serving_unit}"

        // Use highres if available, otherwise thumb
        val imageUrl = food.photo.highres ?: food.photo.thumb
        Glide.with(this)
            .load(imageUrl)
            .into(binding.ivFoodPhoto)
    }

    private fun recalculateNutrients(food: CommonFood) {
        val customQty = binding.etCustomServing.text.toString().toDoubleOrNull()
        if (customQty == null || customQty <= 0.0) {
            Toast.makeText(this, "Enter a valid serving size", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("FoodDetailActivity", "Recalculating for qty = $customQty")
        viewModel.recalculateNutrients(food, customQty)
    }

    private fun updateNutrientUI(detail: FoodDetail) {
        binding.tvTotalCalories.text =
            "Total Calories: %.2f".format(detail.nf_calories)
        binding.tvTotalCarbs.text =
            "Total Carbohydrates: %.2f".format(detail.nf_total_carbohydrate)
        binding.tvTotalFats.text =
            "Total Fats: %.2f".format(detail.nf_total_fat)
        binding.tvTotalProtein.text =
            "Total Protein: %.2f".format(detail.nf_protein)
        binding.tvServingInfoDetail.text =
            "Serving: ${detail.serving_qty} ${detail.serving_unit} " +
                    "(${detail.serving_weight_grams} g)"
    }
}
