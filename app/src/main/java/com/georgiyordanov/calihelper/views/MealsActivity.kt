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

class MealsActivity : BasicActivity() {

    private lateinit var binding: ActivityMealsBinding
    private lateinit var adapter: MealsAdapter

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
        // set defaults
        binding.radioGroupMealType.check(R.id.rbVegan)
        binding.radioGroupDensity.check(R.id.rbLight)

        binding.radioGroupMealType.setOnCheckedChangeListener { _, checkedId ->
            selectedType = when (checkedId) {
                R.id.rbVegan -> "vegan"
                R.id.rbVegetarian -> "vegetarian"
                R.id.rbOmnivore -> "omnivore"
                else -> "vegan"
            }
            loadMeals()
        }

        binding.radioGroupDensity.setOnCheckedChangeListener { _, checkedId ->
            selectedDensity = when (checkedId) {
                R.id.rbLight -> "light"
                R.id.rbMedium -> "medium"
                R.id.rbDense -> "dense"
                else -> "light"
            }
            loadMeals()
        }
    }

    private fun setupRecycler() {
        adapter = MealsAdapter(emptyList())
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
