package com.georgiyordanov.calihelper.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.georgiyordanov.calihelper.data.models.CommonFood
import com.georgiyordanov.calihelper.network.NutritionixApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoodSearchViewModel @Inject constructor(
    private val api: NutritionixApiService
) : ViewModel() {

    private val _searchResults = MutableLiveData<List<CommonFood>>()
    val searchResults: LiveData<List<CommonFood>> = _searchResults

    fun searchFood(query: String) {
        viewModelScope.launch {
            try {
                Log.d("FoodSearchVM", "Searching for: $query")
                val response = api.searchFood(query)
                if (response.isSuccessful) {
                    _searchResults.value = response.body()?.common.orEmpty()
                } else {
                    Log.e("FoodSearchVM", "API error: ${response.errorBody()?.string()}")
                    _searchResults.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("FoodSearchVM", "Exception during API call", e)
                _searchResults.value = emptyList()
            }
        }
    }
}
