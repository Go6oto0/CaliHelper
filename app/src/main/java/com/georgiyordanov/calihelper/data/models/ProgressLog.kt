package com.georgiyordanov.calihelper.data.models

import java.time.LocalDate

data class ProgressLog(
    val userId: String,
    val date: LocalDate,
    val weight: Float,
    val notes: String
)
