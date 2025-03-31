package com.georgiyordanov.calihelper.views

import android.os.Bundle
import com.georgiyordanov.calihelper.databinding.ActivityCalorieTrackerBinding

class CalorieTrackerActivity : BasicActivity() {
    private lateinit var binding: ActivityCalorieTrackerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalorieTrackerBinding.inflate(layoutInflater)
        // Inject the CalorieTrackerActivity content into the base container.
        basicBinding.contentFrame.addView(binding.root)
    }
}
