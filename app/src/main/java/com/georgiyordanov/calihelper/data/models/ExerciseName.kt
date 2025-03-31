package com.georgiyordanov.calihelper.data.models

data class ExerciseName(
    val id: String,
    val name: String
){
    // So the adapter shows the name when calling toString()
    override fun toString(): String = name
}