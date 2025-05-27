// src/test/java/com/georgiyordanov/calihelper/data/repository/WorkoutSuggestorRepositoryTest.kt
package com.georgiyordanov.calihelper.data.repository

import android.util.Log
import com.georgiyordanov.calihelper.data.models.ExerciseName
import com.georgiyordanov.calihelper.data.models.WorkoutPlan
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class WorkoutSuggestorRepositoryTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var namesCollection: CollectionReference
    private lateinit var suggestionsCollection: CollectionReference
    private lateinit var namesSnapshot: QuerySnapshot
    private lateinit var suggSnapshot: QuerySnapshot
    private lateinit var suggestionsQuery: Query
    private lateinit var repo: WorkoutSuggestorRepository

    @BeforeEach
    fun setUp() {
        // intercept android.util.Log static calls
        mockkStatic(Log::class)

        // stub the 2-arg e() just in case…
        every { Log.e(any(), any<String>()) } returns 0
        // **and** stub the 3-arg overload your catch is using:
        every { Log.e(any(), any<String>(), any<Throwable>()) } returns 0

        // (you don’t really need Log.d here, since this repo only uses e(), but it won’t hurt)
        every { Log.d(any(), any<String>()) } returns 0

        // your existing mocks…
        firestore             = mockk()
        namesCollection       = mockk()
        suggestionsCollection = mockk()
        namesSnapshot         = mockk()
        suggSnapshot          = mockk()
        suggestionsQuery      = mockk()

        every { firestore.collection("exerciseNames") } returns namesCollection
        every { firestore.collection("workoutPlanSuggestions") } returns suggestionsCollection

        repo = WorkoutSuggestorRepository(firestore)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    // --- fetchExerciseNames() ---

    @Test
    fun `fetchExerciseNames returns list on success`() = runBlocking {
        val names = listOf(
            ExerciseName("e1", "Push-up"),
            ExerciseName("e2", "Squat")
        )
        every { namesCollection.get() } returns Tasks.forResult(namesSnapshot)
        every { namesSnapshot.toObjects(ExerciseName::class.java) } returns names

        val result = repo.fetchExerciseNames()
        assertEquals(names, result)
    }

    @Test
    fun `fetchExerciseNames propagates exception`() {
        every { namesCollection.get() } returns Tasks.forException(Exception("fail-names"))

        val ex = assertThrows<Exception> {
            runBlocking { repo.fetchExerciseNames() }
        }
        assertEquals("fail-names", ex.message)
    }

    // --- fetchSuggestions(type) ---

    @Test
    fun `fetchSuggestions returns list on success`() = runBlocking {
        val type = "strength"
        val plans = listOf(
            WorkoutPlan("p1", "strength", /* other fields… */),
            WorkoutPlan("p2", "strength", /* other fields… */)
        )
        // stub the query building
        every { suggestionsCollection.whereEqualTo("type", type) } returns suggestionsQuery
        every { suggestionsQuery.get() } returns Tasks.forResult(suggSnapshot)
        every { suggSnapshot.toObjects(WorkoutPlan::class.java) } returns plans

        val result = repo.fetchSuggestions(type)
        assertEquals(plans, result)
    }

    @Test
    fun `fetchSuggestions propagates exception`() {
        val type = "cardio"
        every { suggestionsCollection.whereEqualTo("type", type) } returns suggestionsQuery
        every { suggestionsQuery.get() } returns Tasks.forException(Exception("fail-suggestions"))

        val ex = assertThrows<Exception> {
            runBlocking { repo.fetchSuggestions(type) }
        }
        assertEquals("fail-suggestions", ex.message)
    }
}
