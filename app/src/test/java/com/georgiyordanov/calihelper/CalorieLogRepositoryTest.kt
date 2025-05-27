// src/test/java/com/georgiyordanov/calihelper/data/repository/CalorieLogRepositoryTest.kt
package com.georgiyordanov.calihelper.data.repository

import android.util.Log
import com.georgiyordanov.calihelper.data.models.CalorieLog
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate

class CalorieLogRepositoryTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var collection: CollectionReference
    private lateinit var documentRef: DocumentReference
    private lateinit var snapshot: DocumentSnapshot
    private lateinit var querySnapshot: QuerySnapshot
    private lateinit var repo: CalorieLogRepository

    @BeforeEach
    fun setUp() {
        // Stub Log so it never throws
        mockkStatic(Log::class)
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>(), any<Throwable>()) } returns 0
        every { Log.d(any(), any<String>()) } returns 0

        firestore     = mockk()
        collection    = mockk()
        documentRef   = mockk()
        snapshot      = mockk()
        querySnapshot = mockk()

        every { firestore.collection("calorieLogs") } returns collection
        every { collection.document(any<String>()) } returns documentRef
        every { collection.get() } returns Tasks.forResult(querySnapshot)

        repo = CalorieLogRepository(firestore)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    // --- create() ---

    @Test
    fun `create calls set on new document`() = runBlocking {
        val log = CalorieLog(userId = "u1", caloriesBurned = 10)
        every { collection.document() } returns documentRef
        every { documentRef.set(log) } returns Tasks.forResult(null)

        repo.create(log)

        verify { documentRef.set(log) }
    }

    @Test
    fun `create propagates exception`() {
        val log = CalorieLog(userId = "u2")
        every { collection.document() } returns documentRef
        every { documentRef.set(log) } returns Tasks.forException(Exception("fail-create"))

        val ex = assertThrows<Exception> {
            runBlocking { repo.create(log) }
        }
        assertEquals("fail-create", ex.message)
    }

    // --- read() ---

    @Test
    fun `read existing returns CalorieLog`() = runBlocking {
        val id = "c1"
        val expected = CalorieLog(userId = "u", date = "2025-05-27")
        every { collection.document(id) } returns documentRef
        every { documentRef.get() } returns Tasks.forResult(snapshot)
        every { snapshot.exists() } returns true
        every { snapshot.toObject(CalorieLog::class.java) } returns expected

        val result = repo.read(id)
        assertEquals(expected, result)
    }

    @Test
    fun `read non-existent returns null`() = runBlocking {
        every { documentRef.get() } returns Tasks.forResult(snapshot)
        every { snapshot.exists() } returns false

        val result = repo.read("no")
        assertNull(result)
    }

    @Test
    fun `read propagates exception`() {
        every { documentRef.get() } returns Tasks.forException(Exception("fail-read"))

        val ex = assertThrows<Exception> {
            runBlocking { repo.read("x") }
        }
        assertEquals("fail-read", ex.message)
    }

    // --- readAll() ---

    @Test
    fun `readAll returns list`() = runBlocking {
        val list = listOf(
            CalorieLog(userId = "u"),
            CalorieLog(userId = "v")
        )
        every { querySnapshot.toObjects(CalorieLog::class.java) } returns list

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
    fun `update invokes update on document`() = runBlocking {
        val updates = mapOf("caloriesConsumed" to 200)
        every { documentRef.update(updates) } returns Tasks.forResult(null)

        repo.update("d1", updates)

        verify { documentRef.update(updates) }
    }

    @Test
    fun `update propagates exception`() {
        val updates = emptyMap<String, Any?>()
        every { documentRef.update(updates) } returns Tasks.forException(Exception("fail-update"))

        val ex = assertThrows<Exception> {
            runBlocking { repo.update("d2", updates) }
        }
        assertEquals("fail-update", ex.message)
    }

    // --- updateLog() ---

    @Test
    fun `updateLog invokes update and logs`() = runBlocking {
        val updates = mapOf("netCalories" to 100)
        every { documentRef.update(updates) } returns Tasks.forResult(null)

        repo.updateLog("doc1", updates)

        verify { documentRef.update(updates) }
    }

    @Test
    fun `updateLog propagates exception`() {
        val updates = mapOf("netCalories" to 0)
        every { documentRef.update(updates) } returns Tasks.forException(Exception("fail-ulog"))

        val ex = assertThrows<Exception> {
            runBlocking { repo.updateLog("doc2", updates) }
        }
        assertEquals("fail-ulog", ex.message)
    }

    // --- delete() ---

    @Test
    fun `delete invokes delete`() = runBlocking {
        every { documentRef.delete() } returns Tasks.forResult(null)

        repo.delete("d3")

        verify { documentRef.delete() }
    }

    @Test
    fun `delete propagates exception`() {
        every { documentRef.delete() } returns Tasks.forException(Exception("fail-delete"))

        val ex = assertThrows<Exception> {
            runBlocking { repo.delete("d4") }
        }
        assertEquals("fail-delete", ex.message)
    }

    // --- getOrCreateLogForDate() ---

    @Test
    fun `getOrCreateLogForDate returns existing`() = runBlocking {
        val userId = "u"
        val date = LocalDate.of(2025, 5, 27)
        val docId = "u_2025-05-27"
        val existing = CalorieLog(userId = userId, date = date.toString())
        every { collection.document(docId) } returns documentRef
        every { documentRef.get() } returns Tasks.forResult(snapshot)
        every { snapshot.exists() } returns true
        every { snapshot.toObject(CalorieLog::class.java) } returns existing

        val (log, id) = repo.getOrCreateLogForDate(userId, date)
        assertEquals(existing, log)
        assertEquals(docId, id)
    }

    @Test
    fun `getOrCreateLogForDate creates new when missing`() = runBlocking {
        val userId = "x"
        val date = LocalDate.of(2025, 1, 1)
        val docId = "x_2025-01-01"
        every { collection.document(docId) } returns documentRef
        every { documentRef.get() } returns Tasks.forResult(snapshot)
        every { snapshot.exists() } returns false
        every { documentRef.set(any<CalorieLog>()) } returns Tasks.forResult(null)

        val (log, id) = repo.getOrCreateLogForDate(userId, date)
        assertEquals(id, docId)
        assertEquals(userId, log.userId)
        assertEquals(date.toString(), log.date)
        assertEquals(0, log.caloriesConsumed)
        assertEquals(0, log.caloriesBurned)
        assertEquals(0, log.netCalories)
        assertTrue(log.foodItems.isEmpty())
        verify { documentRef.set(log) }
    }

    @Test
    fun `getOrCreateLogForDate propagates exception`() {
        every { documentRef.get() } returns Tasks.forException(Exception("fail-getOrCreate"))

        val ex = assertThrows<Exception> {
            runBlocking { repo.getOrCreateLogForDate("u", LocalDate.now()) }
        }
        assertEquals("fail-getOrCreate", ex.message)
    }

    // --- addFoodItem() ---

    @Test
    fun `addFoodItem invokes update arrayUnion`() = runBlocking {
        val docId = "d5"
        val item = "food1"
        every { documentRef.update("foodItems", any<FieldValue>()) } returns Tasks.forResult(null)

        repo.addFoodItem(docId, item)

        verify { documentRef.update("foodItems", any<FieldValue>()) }
    }

    @Test
    fun `addFoodItem propagates exception`() {
        every { documentRef.update("foodItems", any<FieldValue>()) } returns Tasks.forException(Exception("fail-add"))

        val ex = assertThrows<Exception> {
            runBlocking { repo.addFoodItem("d6", "item") }
        }
        assertEquals("fail-add", ex.message)
    }
}
