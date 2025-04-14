package com.georgiyordanov.calihelper.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.georgiyordanov.calihelper.data.models.CommonFood
import com.georgiyordanov.calihelper.network.NutritionixApiService
import com.georgiyordanov.calihelper.network.NutritionixRetrofitInstance
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FoodSearchViewModel : ViewModel() {

    private val _searchResults = MutableLiveData<List<CommonFood>>()
    val searchResults: LiveData<List<CommonFood>> = _searchResults


    fun searchFood(query: String) {
        viewModelScope.launch {
            try {
                Log.d("FoodSearch", "Searching for: $query")
                val response = NutritionixRetrofitInstance.api.searchFood(query)
                Log.d("FoodSearch", "Response code: ${response.code()}")
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("FoodSearch", "Received ${body?.common?.size ?: 0} common items")
                    _searchResults.value = body?.common ?: emptyList()
                } else {
                    Log.e("FoodSearch", "API call unsuccessful: ${response.errorBody()?.string()}")
                    _searchResults.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("FoodSearch", "Exception during API call", e)
            }
        }
    }
}
