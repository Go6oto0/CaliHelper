// CalorieTrackerViewModel.kt
package com.georgiyordanov.calihelper.viewmodels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.georgiyordanov.calihelper.data.models.CalorieLog
import com.georgiyordanov.calihelper.data.models.FoodItem
import com.georgiyordanov.calihelper.data.repository.CalorieLogRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class CalorieTrackerViewModel @Inject constructor(
    private val calorieLogRepo: CalorieLogRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _calorieLog = MutableLiveData<CalorieLog>()
    val calorieLog: LiveData<CalorieLog> = _calorieLog

    // Expose the document ID for the current log
    private val _currentLogDocumentId = MutableLiveData<String?>()
    val currentLogDocumentId: LiveData<String?> = _currentLogDocumentId

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val userId = firebaseAuth.currentUser?.uid.orEmpty()
            val today = LocalDate.now()
            val (log, docId) = calorieLogRepo.getOrCreateLogForDate(userId, today)
            _calorieLog.postValue(log)
            _currentLogDocumentId.postValue(docId)
            Log.d("CalorieTrackerVM", "Initialized log for $userId on $today → $docId")
        }
    }

    /**
     * Adds a food item to today's log, updating both local LiveData and Firestore.
     */
    fun updateCalorieLogWithFood(foodItem: FoodItem) {
        val docId = _currentLogDocumentId.value
        if (docId == null) {
            Log.e("CalorieTrackerVM", "No current document ID; cannot update")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val current = _calorieLog.value
            if (current != null) {
                val newConsumed = current.caloriesConsumed + foodItem.calories
                val newNet = newConsumed - current.caloriesBurned
                val updatedLog = current.copy(
                    caloriesConsumed = newConsumed,
                    netCalories = newNet,
                    foodItems = current.foodItems + foodItem
                )
                _calorieLog.postValue(updatedLog)
                val updates = mapOf(
                    "caloriesConsumed" to updatedLog.caloriesConsumed,
                    "netCalories" to updatedLog.netCalories,
                    "foodItems" to updatedLog.foodItems
                )
                Log.d("CalorieTrackerVM", "Applying updates $updates to $docId")
                try {
                    calorieLogRepo.updateLog(docId, updates)
                    Log.d("CalorieTrackerVM", "updateLog succeeded for $docId")
                } catch (e: Exception) {
                    Log.e("CalorieTrackerVM", "updateLog failed; falling back to atomic add", e)
                    try {
                        calorieLogRepo.addFoodItem(docId, foodItem)
                        Log.d("CalorieTrackerVM", "addFoodItem succeeded for $docId")
                    } catch (ex: Exception) {
                        Log.e("CalorieTrackerVM", "addFoodItem fallback failed for $docId", ex)
                    }
                }
            } else {
                Log.e("CalorieTrackerVM", "Local log null; cannot compute update")
            }
        }
    }

    /**
     * Re-fetches today's log from Firestore (or creates it if missing).
     */
    fun refreshCalorieLog() {
        viewModelScope.launch(Dispatchers.IO) {
            val userId = firebaseAuth.currentUser?.uid.orEmpty()
            val today = LocalDate.now()
            val (log, docId) = calorieLogRepo.getOrCreateLogForDate(userId, today)
            _calorieLog.postValue(log)
            _currentLogDocumentId.postValue(docId)
            Log.d("CalorieTrackerVM", "Refreshed log for $userId on $today → $docId")
        }
    }
}
