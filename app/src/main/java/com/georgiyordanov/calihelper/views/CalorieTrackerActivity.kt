// CalorieTrackerActivity.kt
package com.georgiyordanov.calihelper.views

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.georgiyordanov.calihelper.data.models.CommonFood
import com.georgiyordanov.calihelper.data.models.User
import com.georgiyordanov.calihelper.databinding.ActivityCalorieTrackerBinding
import com.georgiyordanov.calihelper.viewmodels.CalorieTrackerViewModel
import com.georgiyordanov.calihelper.viewmodels.FoodSearchViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.O)
class CalorieTrackerActivity : BasicActivity() {

    private lateinit var binding: ActivityCalorieTrackerBinding
    private val calorieTrackerViewModel: CalorieTrackerViewModel by viewModels()
    private val foodSearchViewModel: FoodSearchViewModel by viewModels()

    private lateinit var suggestionsAdapter: ArrayAdapter<String>
    private val commonFoodMap = mutableMapOf<String, CommonFood>()
    private lateinit var foodItemAdapter: FoodItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalorieTrackerBinding.inflate(layoutInflater)
        basicBinding.contentFrame.addView(binding.root)

        // 1) Maintenance info placeholder
        binding.tvMaintenanceInfo.apply {
            text = "Loading maintenance calories…"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        }

        // 2) Food-search dropdown
        suggestionsAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            mutableListOf<String>()
        )
        binding.autoCompleteSearch.setAdapter(suggestionsAdapter)

        // 3) Food-log RecyclerView
        foodItemAdapter = FoodItemAdapter()
        binding.rvFoodItems.apply {
            layoutManager = LinearLayoutManager(this@CalorieTrackerActivity)
            adapter = foodItemAdapter
        }

        setupObservers()
        setupListeners()
        fetchAndShowMaintenance()
    }

    override fun onResume() {
        super.onResume()
        calorieTrackerViewModel.refreshCalorieLog()
        calorieTrackerViewModel.calorieLog.value?.let { log ->
            binding.tvCaloriesConsumed.text =
                "Consumed: ${log.foodItems.sumOf { it.calories }} kcal"
            foodItemAdapter.submitList(log.foodItems)
        }
    }

    private fun setupObservers() {
        // Update consumed total when log changes
        calorieTrackerViewModel.calorieLog.observe(this) { log ->
            val sum = log.foodItems.sumOf { it.calories }
            binding.tvCaloriesConsumed.text = "Consumed: $sum kcal"
            foodItemAdapter.submitList(log.foodItems)
        }

        // Update suggestions dropdown
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

    private fun setupListeners() {
        // As-you-type search
        binding.autoCompleteSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val q = s?.toString()?.trim().orEmpty()
                if (q.isNotBlank()) foodSearchViewModel.searchFood(q)
            }
        })

        // On-select → go to details
        binding.autoCompleteSearch.setOnItemClickListener { parent, _, pos, _ ->
            val name = parent.getItemAtPosition(pos) as String
            commonFoodMap[name]?.let { food ->
                // 1) block further input & show spinner
                binding.autoCompleteSearch.isEnabled = false
                binding.progressBar.visibility = View.VISIBLE

                // 2) grab the actual String value out of your LiveData
                val docId = calorieTrackerViewModel.currentLogDocumentId.value
                if (docId == null) {
                    Toast.makeText(this, "Log not ready yet", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                    binding.autoCompleteSearch.isEnabled = true
                    return@setOnItemClickListener
                }

                // 3) launch details screen, passing Serializable + String
                Intent(this, FoodDetailActivity::class.java).apply {
                    putExtra("selectedFood", food)
                    putExtra("logDocId", docId)
                }.also { startActivity(it) }

                // 4) cleanup UI
                binding.progressBar.visibility = View.GONE
                binding.autoCompleteSearch.isEnabled = true
            } ?: Toast.makeText(this, "Food details not found", Toast.LENGTH_SHORT).show()
        }
    }

    /** Fetch the user’s profile, compute maintenance cals, and show it. */
    private fun fetchAndShowMaintenance() {
        val fbUser = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        CoroutineScope(Dispatchers.Main).launch {
            val snap = db.collection("users")
                .document(fbUser.uid)
                .get()
                .await()
            val user = snap.toObject(User::class.java)

            binding.tvMaintenanceInfo.text = when {
                user == null -> "Unable to load profile data."
                user.gender == null ->
                    "Please set your gender in your profile to calculate maintenance calories."
                user.weight == null || user.height == null || user.age == null ->
                    "Please complete weight, height, and age in your profile."
                else -> {
                    val mCal = calculateMaintenance(
                        gender   = if (user.gender.equals("male", true)) Gender.MALE else Gender.FEMALE,
                        weightKg = user.weight,
                        heightCm = user.height,
                        ageYears = user.age
                    ).toInt()
                    buildString {
                        append("Based on your stats, your maintenance calories are approx. ")
                        append("$mCal kcal/day.\n\n")
                        append("Example activities burned:\n")
                        append("• Running (15 min): ~150 kcal\n")
                        append("• Brisk walking (30 min): ~120 kcal\n")
                        append("• Cycling (30 min): ~250 kcal\n")
                        append("• Swimming (30 min): ~200 kcal")
                    }
                }
            }
        }
    }

    /** Mifflin–St Jeor formula × activity factor (default = lightly-active 1.375). */
    private fun calculateMaintenance(
        gender: Gender,
        weightKg: Float,
        heightCm: Float,
        ageYears: Int,
        activityFactor: Double = 1.375
    ): Double {
        val bmr = when (gender) {
            Gender.MALE   -> 10 * weightKg + 6.25 * heightCm - 5 * ageYears + 5
            Gender.FEMALE -> 10 * weightKg + 6.25 * heightCm - 5 * ageYears - 161
        }
        return bmr * activityFactor
    }

    enum class Gender { MALE, FEMALE }
}
