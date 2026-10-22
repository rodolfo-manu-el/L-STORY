package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class StoryRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createProject_and_observeProjects_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = StoryRepository(firestore, auth)

        val projectIdResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createProject(
                title = "Crónicas del Faro",
                genre = "Misterio",
                premise = "Un farero encuentra cartas del siglo siguiente.",
                structureTemplate = "THREE_ACTS",
                protagonistName = "Mateo",
                protagonistSecret = "Sabe quién naufragará mañana.",
                centralConflict = "Salvar el barco sin romper el tiempo."
            )
        }
        assertTrue(projectIdResult.isSuccess)
        val projectId = projectIdResult.getOrThrow()
        assertTrue(projectId.isNotEmpty())

        val emittedProjects = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeProjects(uid).first { list -> list.any { it.id == projectId } }
        }
        assertTrue(emittedProjects.any { it.id == projectId })
    }

    @Test
    fun getProjectById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val aliceRepo = StoryRepository(firestore, auth)
        val projectId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createProject(
                title = "Manuscrito Privado",
                genre = "Fantasía",
                premise = "El grimorio prohibido.",
                structureTemplate = "HERO_JOURNEY",
                protagonistName = "Aria",
                protagonistSecret = "Heredera del fuego.",
                centralConflict = "Derrotar al regente."
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = StoryRepository(firestore, auth)
        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getProjectById(projectId)
        }
        assertTrue(result.isFailure)
        val firestoreEx = result.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }

    @Test
    fun observeProjects_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = StoryRepository(firestore, auth)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeProjects("unauthenticated_uid").first()
            }
            fail("Expected exception for unauthenticated user")
        } catch (e: Exception) {
            val firestoreEx = checkNotNull(e.unwrapFirestoreException()) {
                "Expected nested FirebaseFirestoreException, got $e"
            }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice.writer@test.com"
        const val BOB_EMAIL = "bob.writer@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
