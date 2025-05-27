// src/test/java/com/georgiyordanov/calihelper/data/repository/ExerciseNameRepositoryTest.kt
package com.georgiyordanov.calihelper.data.repository

import android.util.Log
import com.georgiyordanov.calihelper.data.models.ExerciseName
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class ExerciseNameRepositoryTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var collection: CollectionReference
    private lateinit var documentRef: DocumentReference
    private lateinit var snapshot: DocumentSnapshot
    private lateinit var querySnapshot: QuerySnapshot
    private lateinit var repo: ExerciseNameRepository

    @BeforeEach
    fun setUp() {
        // Stub out android.util.Log
        mockkStatic(Log::class)
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>(), any<Throwable>()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.d(any(), any<String>()) } returns 0

        // Create mocks
        firestore     = mockk()
        collection    = mockk()
        documentRef   = mockk()
        snapshot      = mockk()
        querySnapshot = mockk()

        // Wiring
        every { firestore.collection("exerciseNames") } returns collection
        every { collection.document(any<String>()) } returns documentRef
        every { collection.get() } returns Tasks.forResult(querySnapshot)
        every { querySnapshot.documents } returns emptyList()

        repo = ExerciseNameRepository(firestore)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    // --- create() ---

    @Test
    fun `create calls set on auto‐ID doc`() = runBlocking {
        val entity = ExerciseName(name = "Push-up")
        every { collection.document() } returns documentRef
        every { documentRef.set(entity) } returns Tasks.forResult(null)

        repo.create(entity)

        verify(exactly = 1) { collection.document() }
        verify(exactly = 1) { documentRef.set(entity) }
    }

    @Test
    fun `create propagates exception`() {
        val entity = ExerciseName(name = "Squat")
        every { collection.document() } returns documentRef
        every { documentRef.set(entity) } returns Tasks.forException(Exception("fail-create"))

        val ex = assertThrows<Exception> {
            runBlocking { repo.create(entity) }
        }
        assertEquals("fail-create", ex.message)
    }

    // --- read() ---

    @Test
    fun `read existing returns ExerciseName with id populated`() = runBlocking {
        val id = "ex1"
        val raw = ExerciseName(id = "", name = "Lunge")
        every { collection.document(id) } returns documentRef
        every { documentRef.get() } returns Tasks.forResult(snapshot)
        every { snapshot.exists() } returns true
        every { snapshot.id } returns id
        every { snapshot.toObject(ExerciseName::class.java) } returns raw

        val result = repo.read(id)
        assertNotNull(result)
        assertEquals(id, result!!.id)
        assertEquals("Lunge", result.name)
    }

    @Test
    fun `read non-existent returns null and logs warning`() = runBlocking {
        val id = "ex2"
        every { collection.document(id) } returns documentRef
        every { documentRef.get() } returns Tasks.forResult(snapshot)
        every { snapshot.exists() } returns false

        val result = repo.read(id)
        assertNull(result)
        verify { Log.w("ExerciseNameRepo", "read(): exercise $id not found") }
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
    fun `readAll returns list with ids populated`() = runBlocking {
        val doc1 = mockk<DocumentSnapshot>()
        val doc2 = mockk<DocumentSnapshot>()
        val raw1 = ExerciseName(id = "", name = "Sit-up")
        val raw2 = ExerciseName(id = "", name = "Burpee")
        every { querySnapshot.documents } returns listOf(doc1, doc2)
        every { doc1.id } returns "d1"
        every { doc2.id } returns "d2"
        every { doc1.toObject(ExerciseName::class.java) } returns raw1
        every { doc2.toObject(ExerciseName::class.java) } returns raw2

        val result = repo.readAll()
        assertEquals(2, result!!.size)
        assertEquals(ExerciseName(id = "d1", name = "Sit-up"), result[0])
        assertEquals(ExerciseName(id = "d2", name = "Burpee"), result[1])
        verify { Log.d("ExerciseNameRepo", "readAll(): fetched 2 items") }
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
        val updates = mapOf("name" to "Crunch")
        every { collection.document(id) } returns documentRef
        every { documentRef.update(updates) } returns Tasks.forResult(null)

        repo.update(id, updates)

        verify { documentRef.update(updates) }
    }

    @Test
    fun `update propagates exception`() {
        val id = "u2"
        val updates = mapOf<String, Any?>()
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
