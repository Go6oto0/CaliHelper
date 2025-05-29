// FoodDetailViewModel.kt
package com.georgiyordanov.calihelper.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.georgiyordanov.calihelper.data.models.CommonFood
import com.georgiyordanov.calihelper.data.models.FoodDetail
import com.georgiyordanov.calihelper.data.models.FoodItem
import com.georgiyordanov.calihelper.data.repository.FoodItemRepository
import com.georgiyordanov.calihelper.network.NutritionixApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoodDetailViewModel @Inject constructor(
    private val api: NutritionixApiService,
    private val foodItemRepository: FoodItemRepository
) : ViewModel() {

    private val _nutrientDetails = MutableLiveData<FoodDetail?>()
    val nutrientDetails: LiveData<FoodDetail?> = _nutrientDetails

    /**
     * Recalculates nutrient details using the nutrient endpoint.
     */
    fun recalculateNutrients(food: CommonFood, customServing: Double) {
        val queryString = "$customServing ${food.serving_unit} ${food.food_name}"
        viewModelScope.launch {
            try {
                val response = api.getNutrientInfo(
                    com.georgiyordanov.calihelper.data.models.NutrientRequest(query = queryString)
                )
                if (response.isSuccessful) {
                    _nutrientDetails.value = response.body()?.foods?.firstOrNull()
                } else {
                    _nutrientDetails.value = null
                }
            } catch (e: Exception) {
                Log.e("FoodDetailVM", "Exception during API call", e)
                _nutrientDetails.value = null
            }
        }
    }

    /**
     * Creates a FoodItem using the passed food and nutrient details,
     * and persists it using the FoodItemRepository.
     */
    fun addFoodItemToLog(food: CommonFood, nutrient: FoodDetail) {
        val foodItem = createFoodItem(food, nutrient)
        viewModelScope.launch {
            try {
                foodItemRepository.create(foodItem)
            } catch (e: Exception) {
                Log.e("FoodDetailVM", "Failed to save FoodItem", e)
            }
        }
    }

    /**
     * Pure function to build a FoodItem from API models.
     */
    fun createFoodItem(food: CommonFood, nutrient: FoodDetail): FoodItem {
        return FoodItem(
            name = food.food_name,
            calories = nutrient.nf_calories.toInt(),
            servingSize = "${nutrient.serving_qty} ${nutrient.serving_unit}",
            protein = nutrient.nf_protein.toFloat(),
            fat = nutrient.nf_total_fat.toFloat(),
            carbohydrates = nutrient.nf_total_carbohydrate.toFloat()
        )
    }
}
