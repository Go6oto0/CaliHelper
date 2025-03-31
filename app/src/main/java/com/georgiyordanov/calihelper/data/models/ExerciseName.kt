package com.georgiyordanov.calihelper.data.models

data class ExerciseName(
    var id: String = "",
    var name: String = ""
) {
    override fun toString(): String = name
}
