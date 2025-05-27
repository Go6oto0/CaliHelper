// src/test/java/com/georgiyordanov/calihelper/data/repository/FoodItemRepositoryTest.kt
package com.georgiyordanov.calihelper.data.repository

import android.util.Log
import com.georgiyordanov.calihelper.data.models.FoodItem
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class FoodItemRepositoryTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var collection: CollectionReference
    private lateinit var documentRef: DocumentReference
    private lateinit var snapshot: DocumentSnapshot
    private lateinit var querySnapshot: QuerySnapshot
    private lateinit var repo: FoodItemRepository

    @BeforeEach
    fun setUp() {
        // Stub out android.util.Log so it never throws
        mockkStatic(Log::class)
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>(), any<Throwable>()) } returns 0

        // Core mocks
        firestore     = mockk()
        collection    = mockk()
        documentRef   = mockk()
        snapshot      = mockk()
        querySnapshot = mockk()

        // Wiring
        every { firestore.collection("foodItems") } returns collection
        every { collection.document(any<String>()) } returns documentRef

        repo = FoodItemRepository(firestore)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    // --- create() ---

    @Test
    fun `create calls set on new document`() = runBlocking {
        val item = FoodItem(id = "ignored", name = "Apple", calories = 95)
        every { collection.document() } returns documentRef
        every { documentRef.set(item) } returns Tasks.forResult(null)

        repo.create(item)

        verify(exactly = 1) { documentRef.set(item) }
    }

    @Test
    fun `create propagates exception`() {
        val item = FoodItem(name = "Banana")
        every { collection.document() } returns documentRef
        every { documentRef.set(item) } returns Tasks.forException(Exception("fail-create"))

        val ex = assertThrows<Exception> {
            runBlocking { repo.create(item) }
        }
        assertEquals("fail-create", ex.message)
    }

    // --- read() ---

    @Test
    fun `read existing returns FoodItem`() = runBlocking {
        val id = "f1"
        val expected = FoodItem(id = id, name = "Carrot", calories = 25)
        every { collection.document(id) } returns documentRef
        every { documentRef.get() } returns Tasks.forResult(snapshot)
        every { snapshot.exists() } returns true
        every { snapshot.toObject(FoodItem::class.java) } returns expected

        val result = repo.read(id)
        assertEquals(expected, result)
    }

    @Test
    fun `read non-existent returns null`() = runBlocking {
        val id = "f2"
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
    fun `readAll returns list on success`() = runBlocking {
        val list = listOf(
            FoodItem(id = "a1", name = "Egg", calories = 78),
            FoodItem(id = "b2", name = "Milk", calories = 42)
        )
        every { collection.get() } returns Tasks.forResult(querySnapshot)
        every { querySnapshot.toObjects(FoodItem::class.java) } returns list

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
        val id = "u1"
        val updates = mapOf("calories" to 100, "name" to "Updated")
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

    // --- delete() ---

    @Test
    fun `delete invokes delete on document`() = runBlocking {
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
