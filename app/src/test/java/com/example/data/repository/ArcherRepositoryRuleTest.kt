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

class ArcherRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createAndObserveTask_authenticatedOwner_succeeds() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = ArcherRepository(firestore)

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createTask(
                title = "Upload video to TikTok",
                category = "Routine",
                dueTime = "05:30 PM",
                priority = "HIGH"
            )
        }
        assertTrue(createResult.isSuccess)
        val taskId = createResult.getOrThrow()
        assertTrue(taskId.isNotEmpty())

        val emittedTasks = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeTasks(aliceUid).first { list -> list.any { it.id == taskId } }
        }
        assertTrue(emittedTasks.any { it.id == taskId && it.title == "Upload video to TikTok" })
    }

    @Test
    fun createMeetingAndMemory_authenticatedOwner_succeeds() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = ArcherRepository(firestore)

        val meetingId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createMeeting(
                title = "Urgent Product Review",
                startTime = "04:00 PM",
                remindBeforeMinutes = 15L,
                isUrgent = true,
                notes = "Discuss release 78"
            ).getOrThrow()
        }

        val memoryId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createMemory(
                content = "Prefers Bengali voice alerts",
                tag = "Preference",
                emotionContext = "Focused"
            ).getOrThrow()
        }

        val meetings = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeMeetings(aliceUid).first { list -> list.any { it.id == meetingId } }
        }
        val memories = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeMemories(aliceUid).first { list -> list.any { it.id == memoryId } }
        }

        assertTrue(meetings.any { it.id == meetingId })
        assertTrue(memories.any { it.id == memoryId })
    }

    @Test
    fun getTaskById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = ArcherRepository(firestore)
        val taskId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createTask("Alice Private Task").getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = ArcherRepository(firestore)
        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getTaskById(aliceUid, taskId)
        }
        assertTrue(result.isFailure)
        val firestoreException = result.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(firestoreException)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
    }

    @Test
    fun observeTasks_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        auth.signOut()
        val repository = ArcherRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeTasks(aliceUid).first()
            }
            fail("Expected PERMISSION_DENIED exception for unauthenticated user")
        } catch (e: Exception) {
            val firestoreException = e.unwrapFirestoreException()
            assertNotNull("Expected nested FirebaseFirestoreException, got $e", firestoreException)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice@archerai.test"
        const val BOB_EMAIL = "bob@archerai.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
