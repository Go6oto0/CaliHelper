package com.georgiyordanov.calihelper.views

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.georgiyordanov.calihelper.data.models.FoodItem
import com.georgiyordanov.calihelper.databinding.ItemFoodBinding

class FoodItemAdapter : ListAdapter<FoodItem, FoodItemAdapter.FoodItemViewHolder>(FoodItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodItemViewHolder {
        val binding = ItemFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class FoodItemViewHolder(private val binding: ItemFoodBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(foodItem: FoodItem) {
            binding.tvFoodName.text = foodItem.name
            binding.tvFoodCalories.text = "Calories: ${foodItem.calories}"
            binding.tvServingSize.text = "Serving: ${foodItem.servingSize}"
            // Optionally display protein, fat, and carbs.
        }
    }

    class FoodItemDiffCallback : DiffUtil.ItemCallback<FoodItem>() {
        override fun areItemsTheSame(oldItem: FoodItem, newItem: FoodItem): Boolean {
            return oldItem.id == newItem.id  // Assuming each FoodItem has a unique id.
        }

        override fun areContentsTheSame(oldItem: FoodItem, newItem: FoodItem): Boolean {
            return oldItem == newItem
        }
    }
}
