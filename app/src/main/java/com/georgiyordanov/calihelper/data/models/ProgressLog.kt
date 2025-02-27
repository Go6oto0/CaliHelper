package com.georgiyordanov.calihelper.data.models

data class ProgressLog(
    val userId: Int,
    val date: String,
    val weight: Float,
    val notes: String
)
