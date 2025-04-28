package com.georgiyordanov.calihelper.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.georgiyordanov.calihelper.data.models.CommonFood
import com.georgiyordanov.calihelper.data.models.FoodDetail
import com.georgiyordanov.calihelper.data.models.FoodItem
import com.georgiyordanov.calihelper.data.models.NutrientRequest
import com.georgiyordanov.calihelper.data.repository.FoodItemRepository
import com.georgiyordanov.calihelper.network.NutritionixRetrofitInstance
import kotlinx.coroutines.launch

class FoodDetailViewModel : ViewModel() {

    private val _nutrientDetails = MutableLiveData<FoodDetail?>()
    val nutrientDetails: LiveData<FoodDetail?> = _nutrientDetails

    private val foodItemRepository = FoodItemRepository()  // assuming you have this repository

    /**
     * Recalculates nutrient details using the nutrient endpoint.
     */
    fun recalculateNutrients(food: CommonFood, customServing: Double) {
        val queryString = "$customServing ${food.serving_unit} ${food.food_name}"
        viewModelScope.launch {
            try {
                val response = NutritionixRetrofitInstance.api.getNutrientInfo(
                    NutrientRequest(query = queryString)
                )
                if (response.isSuccessful) {
                    val detail = response.body()?.foods?.firstOrNull()
                    _nutrientDetails.postValue(detail)
                } else {
                    _nutrientDetails.postValue(null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _nutrientDetails.postValue(null)
            }
        }
    }

    /**
     * Creates a FoodItem using the passed food and nutrient details,
     * and persists it using the FoodItemRepository.
     */
    fun addFoodItemToLog(food: CommonFood, nutrient: FoodDetail) {
        val foodItem = FoodItem(
            name = food.food_name,
            calories = nutrient.nf_calories.toInt(),
            servingSize = "${nutrient.serving_qty} ${nutrient.serving_unit}",
            protein = nutrient.nf_protein.toFloat(),
            fat = nutrient.nf_total_fat.toFloat(),
            carbohydrates = nutrient.nf_total_carbohydrate.toFloat()
        )

        viewModelScope.launch {
            try {
                // Call your repository to persist the FoodItem.
                // You can also add further logic to update your local CalorieLog if needed.
                foodItemRepository.create(foodItem)
                // Optionally, post a success value using LiveData or notify via other means.
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
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
