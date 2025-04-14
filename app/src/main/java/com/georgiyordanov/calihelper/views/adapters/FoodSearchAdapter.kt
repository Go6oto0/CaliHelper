package com.georgiyordanov.calihelper.views

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.georgiyordanov.calihelper.data.models.CommonFood
import com.georgiyordanov.calihelper.databinding.ItemFoodSearchBinding

class FoodSearchAdapter(private val onItemClicked: (CommonFood) -> Unit) :
    ListAdapter<CommonFood, FoodSearchAdapter.FoodViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val binding = ItemFoodSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClicked)
    }

    class FoodViewHolder(private val binding: ItemFoodSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(food: CommonFood, onItemClicked: (CommonFood) -> Unit) {
            binding.tvFoodName.text = food.food_name
            binding.tvServingUnit.text = food.serving_unit
            binding.root.setOnClickListener { onItemClicked(food) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CommonFood>() {
        override fun areItemsTheSame(oldItem: CommonFood, newItem: CommonFood) =
            oldItem.tag_id == newItem.tag_id

        override fun areContentsTheSame(oldItem: CommonFood, newItem: CommonFood) =
            oldItem == newItem
    }
}
