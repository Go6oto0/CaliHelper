package com.georgiyordanov.calihelper.views

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.georgiyordanov.calihelper.data.models.CommonFood
import com.georgiyordanov.calihelper.databinding.ActivityCalorieTrackerBinding
import com.georgiyordanov.calihelper.viewmodels.CalorieTrackerViewModel
import com.georgiyordanov.calihelper.viewmodels.FoodSearchViewModel

@RequiresApi(Build.VERSION_CODES.O)
class CalorieTrackerActivity : BasicActivity() {

    private lateinit var binding: ActivityCalorieTrackerBinding
    private val calorieTrackerViewModel: CalorieTrackerViewModel by viewModels()
    private val foodSearchViewModel: FoodSearchViewModel by viewModels()

    // AutoCompleteTextView adapter for dropdown suggestions.
    private lateinit var suggestionsAdapter: ArrayAdapter<String>
    // Map to store food name to its details for quick retrieval.
    private val commonFoodMap = mutableMapOf<String, CommonFood>()

    // Adapter for FoodItems list (the items in your calorie log)
    private lateinit var foodItemAdapter: FoodItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalorieTrackerBinding.inflate(layoutInflater)
        basicBinding.contentFrame.addView(binding.root)

        // Setup autocomplete adapter.
        suggestionsAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, mutableListOf())
        binding.autoCompleteSearch.setAdapter(suggestionsAdapter)

        // Setup FoodItems RecyclerView adapter.
        foodItemAdapter = FoodItemAdapter()
        binding.rvFoodItems.layoutManager = LinearLayoutManager(this)
        binding.rvFoodItems.adapter = foodItemAdapter

        setupObservers()
        setupListeners()
    }
    override fun onResume() {
        super.onResume()
        // If needed, you can trigger a refresh.
        // For instance, you could call a refresh function in your ViewModel,
        // or simply re-set the adapter's list from the currently observed _calorieLog.value.
        calorieTrackerViewModel.refreshCalorieLog()
        calorieTrackerViewModel.calorieLog.value?.let {
            foodItemAdapter.submitList(it.foodItems)
            binding.tvCaloriesConsumed.text = "Consumed: ${it.foodItems.sumOf { food -> food.calories }}"
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupObservers() {
        // Observe changes to the CalorieLog.
        calorieTrackerViewModel.calorieLog.observe(this) { log ->
            // Calculate the sum of calories from the food items array.
            val sumCalories = log.foodItems.sumOf { it.calories }
            binding.tvCaloriesConsumed.text = "Consumed: $sumCalories"

            // Update the FoodItems list in the RecyclerView.
            foodItemAdapter.submitList(log.foodItems)
        }

        foodSearchViewModel.searchResults.observe(this) { results ->
            suggestionsAdapter.clear()
            commonFoodMap.clear()
            results.forEach { food ->
                suggestionsAdapter.add(food.food_name)
                commonFoodMap[food.food_name] = food
            }
            suggestionsAdapter.notifyDataSetChanged()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupListeners() {
        // Listen for text changes in the AutoCompleteTextView.
        binding.autoCompleteSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.isNotEmpty()) {
                    foodSearchViewModel.searchFood(query)
                }
            }
        })

        // When a dropdown suggestion is clicked.
        binding.autoCompleteSearch.setOnItemClickListener { parent, view, position, id ->
            val selectedFoodName = parent.getItemAtPosition(position) as String
            val selectedFood = commonFoodMap[selectedFoodName]
            if (selectedFood != null) {
                val intent = Intent(this, FoodDetailActivity::class.java)
                intent.putExtra("selectedFood", selectedFood)
                // Pass the current log document ID from calorieTrackerViewModel.
                intent.putExtra("logDocId", calorieTrackerViewModel.currentLogDocumentId)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Food item details not found", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
