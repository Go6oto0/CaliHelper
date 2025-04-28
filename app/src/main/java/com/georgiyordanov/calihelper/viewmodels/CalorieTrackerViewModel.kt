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
import kotlinx.coroutines.launch
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
class CalorieTrackerViewModel : ViewModel() {

    private val _calorieLog = MutableLiveData<CalorieLog>()
    val calorieLog: LiveData<CalorieLog> = _calorieLog

    // Expose the document ID for the current CalorieLog.
    var currentLogDocumentId: String? = null
        private set

    private val calorieLogRepo = CalorieLogRepository()

    init {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown_user"
        viewModelScope.launch {
            val today = LocalDate.now()
            // Assume getOrCreateLogForDate returns a Pair<CalorieLog, String>
            val (log, docId) = calorieLogRepo.getOrCreateLogForDate(currentUserId, today)
            _calorieLog.value = log
            currentLogDocumentId = docId
        }
    }

    suspend fun updateCalorieLogWithFood(foodItem: FoodItem, documentId: String) {
        _calorieLog.value?.let { currentLog ->
            Log.d("CalorieTracker", "Current log before update: $currentLog")
            val updatedList = currentLog.foodItems + foodItem
            val newCaloriesConsumed = currentLog.caloriesConsumed + foodItem.calories
            val newLog = currentLog.copy(
                caloriesConsumed = newCaloriesConsumed,
                netCalories = newCaloriesConsumed - currentLog.caloriesBurned,
                foodItems = updatedList
            )
            Log.d("CalorieTracker", "New log after adding food item: $newLog")
            _calorieLog.postValue(newLog)

            val updates = mapOf(
                "caloriesConsumed" to newLog.caloriesConsumed,
                "netCalories" to newLog.netCalories,
                "foodItems" to newLog.foodItems
            )
            Log.d("CalorieTracker", "Prepared updates: $updates")
            Log.d("CalorieTracker", "Using documentId: $documentId")
            try {
                calorieLogRepo.updateLog(documentId, updates)
                Log.d("CalorieTracker", "Firestore update successful for document: $documentId")
            } catch (e: Exception) {
                Log.e("CalorieTracker", "Error updating Firestore log", e)
            }
        } ?: run {
            Log.e("CalorieTracker", "Local CalorieLog is null, using atomic update fallback")
            try {
                calorieLogRepo.addFoodItem(documentId, foodItem)
                Log.d("CalorieTracker", "Atomic update fallback succeeded for document: $documentId")
            } catch (e: Exception) {
                Log.e("CalorieTracker", "Atomic update fallback failed", e)
            }
        }
    }
    fun refreshCalorieLog() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown_user"
        viewModelScope.launch {
            val today = LocalDate.now()
            // Re-fetch the log from Firestore
            val (log, docId) = calorieLogRepo.getOrCreateLogForDate(currentUserId, today)
            _calorieLog.value = log
            currentLogDocumentId = docId
            Log.d("CalorieTracker", "CalorieLog refreshed: $log")
        }
    }


}
