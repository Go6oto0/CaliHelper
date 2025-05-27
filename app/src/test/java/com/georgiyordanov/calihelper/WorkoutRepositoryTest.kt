// src/test/java/com/georgiyordanov/calihelper/data/repository/WorkoutPlanRepositoryTest.kt
package com.georgiyordanov.calihelper.data.repository

import android.util.Log
import com.georgiyordanov.calihelper.data.models.WorkoutPlan
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class WorkoutPlanRepositoryTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var collection: CollectionReference
    private lateinit var documentRef: DocumentReference
    private lateinit var snapshot: DocumentSnapshot
    private lateinit var querySnapshot: QuerySnapshot
    private lateinit var repo: WorkoutPlanRepository

    @BeforeEach
    fun setUp() {
        // 1) stub out android.util.Log so it doesn't crash
        mockkStatic(Log::class)
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>(), any<Throwable>()) } returns 0
        every { Log.d(any(), any<String>()) } returns 0

        // 2) create mocks
        firestore    = mockk()
        collection   = mockk()
        documentRef  = mockk()
        snapshot     = mockk()
        querySnapshot= mockk()

        // 3) default wiring: firestore.collection(...) → collection, collection.document(id) → documentRef
        every { firestore.collection("workoutPlans") } returns collection
        every { collection.document(any<String>()) } returns documentRef

        repo = WorkoutPlanRepository(firestore)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    // --- create() tests ---

    @Test
    fun `create with blank id sets new id`() = runBlocking {
        val plan = WorkoutPlan(id = "", name = "Alpha")
        every { collection.document() } returns documentRef
        every { documentRef.id } returns "generated-id"

        val slot = slot<WorkoutPlan>()
        every { documentRef.set(capture(slot)) } returns Tasks.forResult(null)

        repo.create(plan)

        assertEquals("generated-id", slot.captured.id)
        assertEquals("Alpha", slot.captured.name)
        verify(exactly = 1) { documentRef.set(any()) }
    }

    @Test
    fun `create with existing id uses that id`() = runBlocking {
        val plan = WorkoutPlan(id = "foo", name = "Beta")
        every { collection.document("foo") } returns documentRef
        every { documentRef.set(plan) } returns Tasks.forResult(null)

        repo.create(plan)

        verify { documentRef.set(plan) }
    }

    @Test
    fun `create propagates exception`() {
        val plan = WorkoutPlan(id = "", name = "Gamma")
        every { collection.document() } returns documentRef
        every { documentRef.id } returns "x"
        every { documentRef.set(any()) } returns Tasks.forException(Exception("fail-create"))

        val ex = assertThrows<Exception> {
            runBlocking { repo.create(plan) }
        }
        assertEquals("fail-create", ex.message)
    }

    // --- read() tests ---

    @Test
    fun `read existing returns object`() = runBlocking {
        val id = "r1"
        val expected = WorkoutPlan(id = id, name = "ReadOne")
        every { collection.document(id) } returns documentRef
        every { documentRef.get() } returns Tasks.forResult(snapshot)
        every { snapshot.toObject(WorkoutPlan::class.java) } returns expected
        every { snapshot.exists() } returns true

        val result = repo.read(id)
        assertEquals(expected, result)
    }

    @Test
    fun `read non-existent returns null`() = runBlocking {
        val id = "r2"
        every { collection.document(id) } returns documentRef
        every { documentRef.get() } returns Tasks.forResult(snapshot)
        every { snapshot.toObject(WorkoutPlan::class.java) } returns null
        every { snapshot.exists() } returns false

        val result = repo.read(id)
        assertNull(result)
    }

    @Test
    fun `read propagates exception`() {
        every { collection.document("err") } returns documentRef
        every { documentRef.get() } returns Tasks.forException(Exception("fail-read"))

        val ex = assertThrows<Exception> {
            runBlocking { repo.read("err") }
        }
        assertEquals("fail-read", ex.message)
    }

    // --- readAll() tests ---

    @Test
    fun `readAll returns list`() = runBlocking {
        val list = listOf(
            WorkoutPlan("a", "One"),
            WorkoutPlan("b", "Two")
        )
        every { collection.get() } returns Tasks.forResult(querySnapshot)
        every { querySnapshot.toObjects(WorkoutPlan::class.java) } returns list

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

    // --- update() tests ---

    @Test
    fun `update invokes update with map`() = runBlocking {
        val id = "u1"
        val updates = mapOf("foo" to "bar")
        every { collection.document(id) } returns documentRef
        every { documentRef.update(updates) } returns Tasks.forResult(null)

        repo.update(id, updates)

        verify { documentRef.update(updates) }
    }

    @Test
    fun `update propagates exception`() {
        val id = "u2"
        val updates = emptyMap<String, Any?>()
        every { collection.document(id) } returns documentRef
        every { documentRef.update(updates) } returns Tasks.forException(Exception("fail-update"))

        val ex = assertThrows<Exception> {
            runBlocking { repo.update(id, updates) }
        }
        assertEquals("fail-update", ex.message)
    }

    // --- delete() tests ---

    @Test
    fun `delete invokes delete`() = runBlocking {
        val id = "d1"
        every { collection.document(id) } returns documentRef
        every { documentRef.delete() } returns Tasks.forResult(null)

        repo.delete(id)

        verify { documentRef.delete() }
    }

    @Test
    fun `delete propagates exception`() {
        val id = "d2"
        every { collection.document(id) } returns documentRef
        every { documentRef.delete() } returns Tasks.forException(Exception("fail-delete"))

        val ex = assertThrows<Exception> {
            runBlocking { repo.delete(id) }
        }
        assertEquals("fail-delete", ex.message)
    }
}
