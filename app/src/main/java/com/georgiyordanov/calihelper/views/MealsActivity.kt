package com.georgiyordanov.calihelper.views

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.georgiyordanov.calihelper.R
import com.georgiyordanov.calihelper.data.models.Meal
import com.georgiyordanov.calihelper.databinding.ActivityMealsBinding
import com.georgiyordanov.calihelper.views.adapters.MealsAdapter
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MealsActivity : BasicActivity() {

    private lateinit var binding: ActivityMealsBinding
    @Inject
    lateinit var adapter: MealsAdapter

    // current filter selections
    private var selectedType: String = "vegan"
    private var selectedDensity: String = "light"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // inflate your layout into the BasicActivity container
        binding = ActivityMealsBinding.inflate(layoutInflater)
        basicBinding.contentFrame.addView(binding.root)

        // edge-to-edge
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        setupFilters()
        setupRecycler()
        loadMeals()
    }

    private fun setupFilters() {
        // Default‐select the first chip in each group
        binding.chipGroupMealType.check(R.id.chipVegan)
        binding.chipGroupDensity.check(R.id.chipLight)

        // Meal‐type selection listener
        binding.chipGroupMealType.setOnCheckedChangeListener { _, checkedId ->
            selectedType = when (checkedId) {
                R.id.chipVegan      -> "vegan"
                R.id.chipVegetarian -> "vegetarian"
                R.id.chipOmnivore   -> "omnivore"
                else                 -> "vegan"
            }
            loadMeals()
        }

        // Density selection listener
        binding.chipGroupDensity.setOnCheckedChangeListener { _, checkedId ->
            selectedDensity = when (checkedId) {
                R.id.chipLight  -> "light"
                R.id.chipMedium -> "medium"
                R.id.chipDense  -> "dense"
                else            -> "light"
            }
            loadMeals()
        }
    }


    private fun setupRecycler() {
        binding.rvMeals.layoutManager = LinearLayoutManager(this)
        binding.rvMeals.adapter = adapter
    }

    private fun loadMeals() {
        val db = FirebaseFirestore.getInstance()
        db.collection("meals")
            .whereEqualTo("dietType", selectedType)
            .whereEqualTo("density", selectedDensity)
            .get()
            .addOnSuccessListener { snapshot ->
                val meals = snapshot.toObjects(Meal::class.java)
                adapter.updateData(meals)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading meals: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
