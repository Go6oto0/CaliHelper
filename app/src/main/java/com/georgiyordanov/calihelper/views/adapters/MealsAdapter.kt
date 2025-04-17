package com.georgiyordanov.calihelper.views.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.georgiyordanov.calihelper.R
import com.georgiyordanov.calihelper.data.models.Meal
import com.georgiyordanov.calihelper.views.MealDetailActivity

class MealsAdapter(private var meals: List<Meal>) :
    RecyclerView.Adapter<MealsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvMealName)
        val tvDesc: TextView = view.findViewById(R.id.tvMealDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meal, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val meal = meals[position]
        holder.tvName.text = meal.name
        holder.tvDesc.text = meal.description

        // Launch detail activity on click, passing the Meal as Serializable
        holder.itemView.setOnClickListener {
            val ctx = holder.itemView.context
            val intent = Intent(ctx, MealDetailActivity::class.java).apply {
                putExtra("meal", meal)
            }
            ctx.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = meals.size

    fun updateData(newMeals: List<Meal>) {
        meals = newMeals
        notifyDataSetChanged()
    }
}
