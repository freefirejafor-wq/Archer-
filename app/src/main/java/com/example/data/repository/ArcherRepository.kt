package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.OperationType
import com.example.data.handleFirestoreError
import com.example.data.model.MeetingAlert
import com.example.data.model.MemoryEntry
import com.example.data.model.MoodLog
import com.example.data.model.TaskItem
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ArcherRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- TASKS & DAILY ROUTINES ---

    fun observeTasks(userId: String = requireUserId()): Flow<List<TaskItem>> = flow {
        val path = "users/$userId/tasks"
        emitAll(
            db.collection("users").document(userId).collection("tasks")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        TaskItem::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun createTask(
        title: String,
        category: String = "Routine",
        dueTime: String = "10:00 AM",
        priority: String = "HIGH"
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val tasksRef = db.collection("users").document(uid).collection("tasks")
        val docRef = tasksRef.document()
        val payload = mapOf(
            "userId" to uid,
            "title" to title.trim().take(300).ifEmpty { "New Task" },
            "category" to category.trim().take(60).ifEmpty { "Routine" },
            "dueTime" to dueTime.trim().take(60).ifEmpty { "10:00 AM" },
            "completed" to false,
            "priority" to if (priority in listOf("HIGH", "MEDIUM", "LOW")) priority else "HIGH",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun getTaskById(userOwnerId: String, taskId: String): Result<TaskItem?> = runCatching {
        val docRef = db.collection("users").document(userOwnerId).collection("tasks").document(taskId)
        try {
            val snapshot = docRef.get().await()
            snapshot.toObject(TaskItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun toggleTaskCompleted(task: TaskItem) {
        val uid = requireUserId()
        require(task.id.isNotBlank()) { "Task ID must not be blank for update" }
        requireNotNull(task.createdAt) { "Task createdAt must not be null for update" }
        val docRef = db.collection("users").document(uid).collection("tasks").document(task.id)
        try {
            docRef.set(task.copy(completed = !task.completed, updatedAt = null)).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteTask(taskId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("tasks").document(taskId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- MEETINGS & PROACTIVE ALERTS ---

    fun observeMeetings(userId: String = requireUserId()): Flow<List<MeetingAlert>> = flow {
        val path = "users/$userId/meetings"
        emitAll(
            db.collection("users").document(userId).collection("meetings")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        MeetingAlert::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun createMeeting(
        title: String,
        startTime: String,
        remindBeforeMinutes: Long = 15L,
        isUrgent: Boolean = true,
        notes: String = ""
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("meetings").document()
        val payload = mapOf(
            "userId" to uid,
            "title" to title.trim().take(300).ifEmpty { "Important Meeting" },
            "startTime" to startTime.trim().take(60).ifEmpty { "04:00 PM" },
            "remindBeforeMinutes" to remindBeforeMinutes.coerceIn(0L, 1440L),
            "isUrgent" to isUrgent,
            "notes" to notes.trim().take(2000),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteMeeting(meetingId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("meetings").document(meetingId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- SMART MEMORY VAULT & VOICE NOTES ---

    fun observeMemories(userId: String = requireUserId()): Flow<List<MemoryEntry>> = flow {
        val path = "users/$userId/memories"
        emitAll(
            db.collection("users").document(userId).collection("memories")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        MemoryEntry::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun createMemory(
        content: String,
        tag: String = "Personal",
        emotionContext: String = "Calm"
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("memories").document()
        val payload = mapOf(
            "userId" to uid,
            "content" to content.trim().take(2000).ifEmpty { "Saved memory note" },
            "tag" to tag.trim().take(60).ifEmpty { "Personal" },
            "emotionContext" to emotionContext.trim().take(60).ifEmpty { "Calm" },
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteMemory(memoryId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("memories").document(memoryId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- MOOD & EMOTION ENGINE LOGS ---

    fun observeMoods(userId: String = requireUserId()): Flow<List<MoodLog>> = flow {
        val path = "users/$userId/moods"
        emitAll(
            db.collection("users").document(userId).collection("moods")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        MoodLog::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun logMood(
        emotion: String,
        soulMode: String,
        note: String,
        energyScore: Long = 85L
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("moods").document()
        val payload = mapOf(
            "userId" to uid,
            "emotion" to emotion.trim().take(60).ifEmpty { "Calm" },
            "soulMode" to soulMode.trim().take(60).ifEmpty { "Empathetic Friend" },
            "note" to note.trim().take(1000),
            "energyScore" to energyScore.coerceIn(1L, 100L),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun ensureStarterDataIfEmpty(userId: String) {
        val tasksPath = "users/$userId/tasks"
        try {
            val existingTasks = db.collection("users").document(userId).collection("tasks")
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()
            if (existingTasks.isEmpty) {
                createTask(
                    title = "Release desktop version 78",
                    category = "Work",
                    dueTime = "02:00 PM",
                    priority = "HIGH"
                )
                createTask(
                    title = "Upload video to TikTok",
                    category = "Routine",
                    dueTime = "05:30 PM",
                    priority = "HIGH"
                )
                createTask(
                    title = "সকালের ব্যায়াম ও ডেইলি ব্রিফিং চেক করা",
                    category = "Routine",
                    dueTime = "08:30 AM",
                    priority = "MEDIUM"
                )
            }

            val existingMeetings = db.collection("users").document(userId).collection("meetings")
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()
            if (existingMeetings.isEmpty) {
                createMeeting(
                    title = "জরুরি প্রজেক্ট ও ক্লায়েন্ট মিটিং (Urgent Sync)",
                    startTime = "04:00 PM",
                    remindBeforeMinutes = 15L,
                    isUrgent = true,
                    notes = "সপ্তাহের কাজের অগ্রগতি ও নতুন ফিচার ডেমো প্রস্তুত রাখা।"
                )
                createMeeting(
                    title = "রাতের রুটিন ও আগামীকালের প্ল্যানিং",
                    startTime = "09:30 PM",
                    remindBeforeMinutes = 30L,
                    isUrgent = false,
                    notes = "সারাদিনের টাস্ক রিভিউ এবং মেমোরি আপডেট।"
                )
            }

            val existingMemories = db.collection("users").document(userId).collection("memories")
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()
            if (existingMemories.isEmpty) {
                createMemory(
                    content = "ব্যবহারকারী বাংলা ও ইংরেজি উভয় ভাষায় বন্ধুসুলভ ভয়েস অ্যাসিস্ট্যান্ট পছন্দ করেন এবং মিটিংয়ের ১৫ মিনিট আগে রিমাইন্ডার চান।",
                    tag = "Preference",
                    emotionContext = "Focused"
                )
                createMemory(
                    content = "প্রতিদিন বিকেলে গ্যালারি থেকে শর্ট ভিডিও TikTok-এ আপলোড করার রুটিন রয়েছে।",
                    tag = "Important",
                    emotionContext = "Happy"
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, tasksPath)
        }
    }
}
