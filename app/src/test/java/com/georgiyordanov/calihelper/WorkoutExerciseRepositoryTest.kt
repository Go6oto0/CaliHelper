// src/test/java/com/georgiyordanov/calihelper/data/repository/WorkoutExerciseRepositoryTest.kt
package com.georgiyordanov.calihelper.data.repository

import android.util.Log
import com.georgiyordanov.calihelper.data.models.WorkoutExercise
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class WorkoutExerciseRepositoryTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var collection: CollectionReference
    private lateinit var documentRef: DocumentReference
    private lateinit var snapshot: DocumentSnapshot
    private lateinit var querySnapshot: QuerySnapshot
    private lateinit var repo: WorkoutExerciseRepository

    @BeforeEach
    fun setUp() {
        // Stub out android.util.Log so it doesn’t crash
        mockkStatic(Log::class)
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>(), any<Throwable>()) } returns 0
        every { Log.d(any(), any<String>()) } returns 0

        // Create mocks
        firestore     = mockk()
        collection    = mockk()
        documentRef   = mockk()
        snapshot      = mockk()
        querySnapshot = mockk()

        // Default wiring
        every { firestore.collection("workoutExercises") } returns collection
        every { collection.document(any<String>()) } returns documentRef

        repo = WorkoutExerciseRepository(firestore)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    // --- create() ---

    @Test
    fun `create with blank id generates new id`() = runBlocking {
        val entity = WorkoutExercise(
            id = "",
            workoutPlanId = "planA",
            exerciseId = "ex1",
            repetitions = 12,
            sets = 3
        )
        every { collection.document() } returns documentRef
        every { documentRef.id } returns "new-ex-id"

        val slot = slot<WorkoutExercise>()
        every { documentRef.set(capture(slot)) } returns Tasks.forResult(null)

        repo.create(entity)

        assertEquals("new-ex-id", slot.captured.id)
        assertEquals("planA", slot.captured.workoutPlanId)
        assertEquals("ex1", slot.captured.exerciseId)
        assertEquals(12, slot.captured.repetitions)
        assertEquals(3, slot.captured.sets)
        verify(exactly = 1) { documentRef.set(any()) }
    }

    @Test
    fun `create with existing id uses that id`() = runBlocking {
        val entity = WorkoutExercise(
            id = "ex123",
            workoutPlanId = "planB",
            exerciseId = "ex2",
            repetitions = 8,
            sets = 4
        )
        every { collection.document("ex123") } returns documentRef
        every { documentRef.set(entity) } returns Tasks.forResult(null)

        repo.create(entity)

        verify { documentRef.set(entity) }
    }

    @Test
    fun `create propagates exception`() {
        val entity = WorkoutExercise(
            id = "",
            workoutPlanId = "planC",
            exerciseId = "ex3",
            repetitions = 5,
            sets = 2
        )
        every { collection.document() } returns documentRef
        every { documentRef.id } returns "err-id"
        every { documentRef.set(any()) } returns Tasks.forException(Exception("fail-create"))

        val ex = assertThrows<Exception> {
            runBlocking { repo.create(entity) }
        }
        assertEquals("fail-create", ex.message)
    }

    // --- read() ---

    @Test
    fun `read existing returns object`() = runBlocking {
        val id = "read1"
        val expected = WorkoutExercise(
            id = id,
            workoutPlanId = "planX",
            exerciseId = "exX",
            repetitions = 10,
            sets = 1
        )
        every { collection.document(id) } returns documentRef
        every { documentRef.get() } returns Tasks.forResult(snapshot)
        every { snapshot.exists() } returns true
        every { snapshot.toObject(WorkoutExercise::class.java) } returns expected

        val result = repo.read(id)
        assertEquals(expected, result)
    }

    @Test
    fun `read non-existent returns null`() = runBlocking {
        val id = "read2"
        every { collection.document(id) } returns documentRef
        every { documentRef.get() } returns Tasks.forResult(snapshot)
        every { snapshot.exists() } returns false

        val result = repo.read(id)
        assertNull(result)
    }

    @Test
    fun `read propagates exception`() {
        every { collection.document("bad") } returns documentRef
        every { documentRef.get() } returns Tasks.forException(Exception("fail-read"))

        val ex = assertThrows<Exception> {
            runBlocking { repo.read("bad") }
        }
        assertEquals("fail-read", ex.message)
    }

    // --- readAll() ---

    @Test
    fun `readAll returns list`() = runBlocking {
        val list = listOf(
            WorkoutExercise("a1", "plan1", "exA", 15, 2),
            WorkoutExercise("b2", "plan2", "exB", 20, 3)
        )
        every { collection.get() } returns Tasks.forResult(querySnapshot)
        every { querySnapshot.toObjects(WorkoutExercise::class.java) } returns list

        val result = repo.readAll()
        assertEquals(list, result)
    }

    @Test
    fun `readAll propagates exception`() {
        every { collection.get() } returns Tasks.forException(Exception("fail-readAll"))

        val ex = assertThrows<Exception> {
            runBlocking { repo.readAll() }
        }
        assertEquals("fail-readAll", ex.message)
    }

    // --- update() ---

    @Test
    fun `update invokes update with map`() = runBlocking {
        val id = "u-1"
        val updates = mapOf("repetitions" to 30, "sets" to 5)
        every { collection.document(id) } returns documentRef
        every { documentRef.update(updates) } returns Tasks.forResult(null)

        repo.update(id, updates)

        verify { documentRef.update(updates) }
    }

    @Test
    fun `update propagates exception`() {
        val id = "u-2"
        val updates = emptyMap<String, Any?>()
        every { collection.document(id) } returns documentRef
        every { documentRef.update(updates) } returns Tasks.forException(Exception("fail-update"))

        val ex = assertThrows<Exception> {
            runBlocking { repo.update(id, updates) }
        }
        assertEquals("fail-update", ex.message)
    }

    // --- delete() ---

    @Test
    fun `delete invokes delete`() = runBlocking {
        val id = "d-1"
        every { collection.document(id) } returns documentRef
        every { documentRef.delete() } returns Tasks.forResult(null)

        repo.delete(id)

        verify { documentRef.delete() }
    }

    @Test
    fun `delete propagates exception`() {
        val id = "d-2"
        every { collection.document(id) } returns documentRef
        every { documentRef.delete() } returns Tasks.forException(Exception("fail-delete"))

        val ex = assertThrows<Exception> {
            runBlocking { repo.delete(id) }
        }
        assertEquals("fail-delete", ex.message)
    }
}
