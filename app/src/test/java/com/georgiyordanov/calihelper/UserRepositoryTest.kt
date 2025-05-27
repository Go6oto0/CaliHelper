// src/test/java/com/georgiyordanov/calihelper/data/repository/UserRepositoryTest.kt
package com.georgiyordanov.calihelper.data.repository

import android.util.Log
import com.georgiyordanov.calihelper.data.models.User
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.firestore.SetOptions
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class UserRepositoryTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var usersCollection: CollectionReference
    private lateinit var documentRef: DocumentReference
    private lateinit var snapshot: DocumentSnapshot
    private lateinit var querySnapshot: QuerySnapshot
    private lateinit var repo: UserRepository

    @BeforeEach
    fun setUp() {
        // Stub Log so it never throws
        mockkStatic(Log::class)
        every { Log.d(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>(), any<Throwable>()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0

        // Core mocks
        firestore       = mockk()
        auth            = mockk()
        usersCollection = mockk()
        documentRef     = mockk()
        snapshot        = mockk()
        querySnapshot   = mockk()

        // Wiring: firestore.collection("users") → usersCollection
        every { firestore.collection("users") } returns usersCollection
        every { usersCollection.document(any<String>()) } returns documentRef

        repo = UserRepository(firestore, auth)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    // --- create() ---

    @Test
    fun `create writes document on success`() = runBlocking {
        // only specify the fields you care about; others use defaults
        val user = User(uid = "u1", email = "a@e.com")
        every { usersCollection.document("u1") } returns documentRef
        every { documentRef.set(user) } returns Tasks.forResult(null)

        repo.create(user)

        verify { documentRef.set(user) }
    }

    @Test
    fun `create propagates exception`() {
        val user = User(uid = "u2", email = "b@e.com")
        every { usersCollection.document("u2") } returns documentRef
        every { documentRef.set(user) } returns Tasks.forException(Exception("fail-create"))

        val ex = assertThrows<Exception> {
            runBlocking { repo.create(user) }
        }
        assertEquals("fail-create", ex.message)
    }

    // --- read() ---

    @Test
    fun `read existing returns User`() = runBlocking {
        val id = "r1"
        val expected = User(
            role = "user",
            uid = id,
            email = "c@e.com",
            profileSetup = false,
            userName = "Charlie",
            weight = 70f,
            height = 175f,
            age = 25,
            gender = "male",
            goal = "gain"
        )
        every { usersCollection.document(id) } returns documentRef
        every { documentRef.get() } returns Tasks.forResult(snapshot)
        every { snapshot.exists() } returns true
        every { snapshot.toObject(User::class.java) } returns expected

        val result = repo.read(id)
        assertEquals(expected, result)
    }

    @Test
    fun `read non-existent returns null and logs warning`() = runBlocking {
        val id = "r2"
        every { usersCollection.document(id) } returns documentRef
        every { documentRef.get() } returns Tasks.forResult(snapshot)
        every { snapshot.exists() } returns false

        val result = repo.read(id)
        assertNull(result)
        verify { Log.w("UserRepository", "read(): user $id not found") }
    }

    @Test
    fun `read propagates exception`() {
        every { usersCollection.document("bad") } returns documentRef
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
            User(uid = "x1", email = "x@e.com"),
            User(uid = "x2", email = "y@e.com")
        )
        every { usersCollection.get() } returns Tasks.forResult(querySnapshot)
        every { querySnapshot.toObjects(User::class.java) } returns list

        val result = repo.readAll()
        assertEquals(list, result)
    }

    @Test
    fun `readAll propagates exception`() {
        every { usersCollection.get() } returns Tasks.forException(Exception("fail-readAll"))

        val ex = assertThrows<Exception> {
            runBlocking { repo.readAll() }
        }
        assertEquals("fail-readAll", ex.message)
    }

    // --- update() ---

    @Test
    fun `update merges fields on success`() = runBlocking {
        val updates = mapOf("userName" to "NewName", "profileSetup" to true)
        every { usersCollection.document("u3") } returns documentRef
        every { documentRef.set(updates, SetOptions.merge()) } returns Tasks.forResult(null)

        repo.update("u3", updates)

        verify { documentRef.set(updates, SetOptions.merge()) }
    }

    @Test
    fun `update propagates exception`() {
        val updates = mapOf<String, Any?>()
        every { usersCollection.document("u4") } returns documentRef
        every { documentRef.set(updates, SetOptions.merge()) }
            .returns(Tasks.forException(Exception("fail-update")))

        val ex = assertThrows<Exception> {
            runBlocking { repo.update("u4", updates) }
        }
        assertEquals("fail-update", ex.message)
    }

    // --- delete() ---

    @Test
    fun `delete removes document on success`() = runBlocking {
        every { usersCollection.document("u5") } returns documentRef
        every { documentRef.delete() } returns Tasks.forResult(null)

        repo.delete("u5")

        verify { documentRef.delete() }
    }

    @Test
    fun `delete propagates exception`() {
        every { usersCollection.document("u6") } returns documentRef
        every { documentRef.delete() } returns Tasks.forException(Exception("fail-delete"))

        val ex = assertThrows<Exception> {
            runBlocking { repo.delete("u6") }
        }
        assertEquals("fail-delete", ex.message)
    }

    // --- currentUserId() ---

    @Test
    fun `currentUserId returns uid when signed in`() {
        val user = mockk<FirebaseUser>()
        every { auth.currentUser } returns user
        every { user.uid } returns "signedInUid"

        assertEquals("signedInUid", repo.currentUserId())
    }

    @Test
    fun `currentUserId returns null when no user`() {
        every { auth.currentUser } returns null

        assertNull(repo.currentUserId())
    }
}
