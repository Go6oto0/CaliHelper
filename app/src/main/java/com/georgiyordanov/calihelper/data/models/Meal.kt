package com.georgiyordanov.calihelper.data.models

import java.io.Serializable


data class Meal(
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    val dietType: String = "",
    val density: String = "",
    val ingredients: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val imageUrl: String? = null,
    val calories: Int? = null
) : Serializable
