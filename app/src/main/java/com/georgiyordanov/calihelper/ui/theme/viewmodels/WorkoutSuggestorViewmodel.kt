package com.georgiyordanov.calihelper.ui.theme.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.georgiyordanov.calihelper.data.models.ExerciseName
import com.georgiyordanov.calihelper.data.models.WorkoutPlan
import com.georgiyordanov.calihelper.data.repository.WorkoutSuggestorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WorkoutSuggestorViewModel : ViewModel() {
    private val repo = WorkoutSuggestorRepository()

    private val _exerciseNames = MutableStateFlow<List<ExerciseName>>(emptyList())
    val exerciseNames: StateFlow<List<ExerciseName>> = _exerciseNames

    private val _suggestions = MutableStateFlow<List<WorkoutPlan>>(emptyList())
    val suggestions: StateFlow<List<WorkoutPlan>> = _suggestions

    /** Kick off loading all exercise names. */
    fun loadExerciseNames() {
        viewModelScope.launch {
            _exerciseNames.value = repo.fetchExerciseNames()
        }
    }

    /** Kick off loading suggestions for a given category. */
    fun loadSuggestions(type: String) {
        viewModelScope.launch {
            _suggestions.value = repo.fetchSuggestions(type)
        }
    }
}
