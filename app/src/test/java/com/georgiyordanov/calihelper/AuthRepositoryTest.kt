
package com.georgiyordanov.calihelper

import com.georgiyordanov.calihelper.data.repository.AuthRepository
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
class AuthRepositoryTest {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var repo: AuthRepository

    @BeforeEach
    fun setUp() {
        firebaseAuth = mockk()
        repo = AuthRepository(firebaseAuth)
    }

    @Test
    fun `signUp returns success when user created`() = runBlocking {
        val email = "test@example.com"
        val password = "pass123"
        val authResult = mockk<AuthResult>()
        val user = mockk<FirebaseUser>()

        every { firebaseAuth.createUserWithEmailAndPassword(email, password) } returns Tasks.forResult(authResult)
        every { authResult.user } returns user

        val result = repo.signUp(email, password)
        assertTrue(result.isSuccess)
        assertEquals(user, result.getOrNull())
    }

    @Test
    fun `signUp returns failure when user is null`() = runBlocking {
        val email = "test@example.com"
        val password = "pass123"
        val authResult = mockk<AuthResult>()

        every { firebaseAuth.createUserWithEmailAndPassword(email, password) } returns Tasks.forResult(authResult)
        every { authResult.user } returns null

        val result = repo.signUp(email, password)
        assertTrue(result.isFailure)
        assertEquals("Firebase returned null user", result.exceptionOrNull()?.message)
    }

    @Test
    fun `signUp returns failure when exception thrown`() = runBlocking {
        val email = "x@e.com"
        val password = "pw"
        val ex = Exception("fail-signUp")
        every { firebaseAuth.createUserWithEmailAndPassword(email, password) } returns Tasks.forException(ex)

        val result = repo.signUp(email, password)
        assertTrue(result.isFailure)
        assertSame(ex, result.exceptionOrNull())
    }

    @Test
    fun `signIn returns success on valid credentials`() = runBlocking {
        val email = "u@a.com"
        val password = "pw"
        val authResult = mockk<AuthResult>()
        every { firebaseAuth.signInWithEmailAndPassword(email, password) } returns Tasks.forResult(authResult)

        val result = repo.signIn(email, password)
        assertTrue(result.isSuccess)
        assertNull(result.exceptionOrNull())
        assertEquals(Unit, result.getOrNull())
    }

    @Test
    fun `signIn returns failure on FirebaseAuthException`() = runBlocking {
        val email = "e@e.com"
        val password = "p"
        val fae = mockk<FirebaseAuthException>()
        every { firebaseAuth.signInWithEmailAndPassword(email, password) } returns Tasks.forException(fae)

        val result = repo.signIn(email, password)
        assertTrue(result.isFailure)
        assertSame(fae, result.exceptionOrNull())
    }

    @Test
    fun `signIn returns failure on generic exception`() = runBlocking {
        val email = "e@e.com"
        val password = "p"
        val ex = RuntimeException("fail-signIn")
        every { firebaseAuth.signInWithEmailAndPassword(email, password) } returns Tasks.forException(ex)

        val result = repo.signIn(email, password)
        assertTrue(result.isFailure)
        assertSame(ex, result.exceptionOrNull())
    }

    @Test
    fun `getCurrentUser returns current user or null`() {
        val user = mockk<FirebaseUser>()
        every { firebaseAuth.currentUser } returns user
        assertEquals(user, repo.getCurrentUser())

        every { firebaseAuth.currentUser } returns null
        assertNull(repo.getCurrentUser())
    }

    @Test
    fun `signOut calls firebaseAuth signOut`() {
        every { firebaseAuth.signOut() } just Runs

        repo.signOut()

        verify(exactly = 1) { firebaseAuth.signOut() }
    }
}