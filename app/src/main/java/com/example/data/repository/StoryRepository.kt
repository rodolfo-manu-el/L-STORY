package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.BetaComment
import com.example.data.model.LessonProgress
import com.example.data.model.StoryChapter
import com.example.data.model.StoryCharacter
import com.example.data.model.StoryProject
import com.example.data.model.TimelineEvent
import com.example.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
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
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write"),
}

fun handleFirestoreError(exception: Exception, operationType: OperationType, path: String?): String {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    val providerInfoList = currentUser?.providerData?.map { provider ->
        JSONObject().apply {
            put("providerId", provider.providerId)
            put("email", provider.email)
        }
    } ?: emptyList()

    val authInfoJson = JSONObject().apply {
        put("userId", currentUser?.uid)
        put("email", currentUser?.email)
        put("emailVerified", currentUser?.isEmailVerified)
        put("tenantId", currentUser?.tenantId)
        put("providerInfo", JSONArray(providerInfoList))
    }

    val errorInfoJson = JSONObject().apply {
        put("error", exception.message ?: exception.toString())
        put("operationType", operationType.value)
        put("path", path)
        put("authInfo", authInfoJson)
    }

    val jsonString = errorInfoJson.toString()
    Log.e("FirestoreError", "Firestore Error: $jsonString")
    return jsonString
}

class StoryRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        FirebaseAuth.getInstance()
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("El usuario debe iniciar sesión con Google antes de acceder a Firestore.")
    }

    // --- USER PROFILE & GAMIFICATION ---

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                    throw error
                }
        )
    }

    suspend fun ensureUserProfile(displayName: String): Result<UserProfile> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                snap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    ?: UserProfile(id = uid, userId = uid, displayName = displayName)
            } else {
                val safeName = displayName.trim().ifBlank { "Autor Novel" }.take(100)
                val payload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "dailyWordGoal" to 500L,
                    "currentStreak" to 1L,
                    "todayWords" to 0L,
                    "completedLessonsCount" to 0L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
                UserProfile(id = uid, userId = uid, displayName = safeName)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun updateWritingGoalAndProgress(
        dailyWordGoal: Long,
        todayWords: Long,
        currentStreak: Long,
        completedLessonsCount: Long
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            docRef.update(
                mapOf(
                    "dailyWordGoal" to dailyWordGoal.coerceIn(50L, 50000L),
                    "todayWords" to todayWords.coerceIn(0L, 200000L),
                    "currentStreak" to currentStreak.coerceIn(0L, 10000L),
                    "completedLessonsCount" to completedLessonsCount.coerceIn(0L, 1000L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- PROJECTS ---

    fun observeProjects(userId: String? = auth.currentUser?.uid): Flow<List<StoryProject>> = flow {
        val targetUid = userId ?: "unauthenticated"
        val path = "projects"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", targetUid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(StoryProject::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.updatedAt?.seconds ?: 0L }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun createProject(
        title: String,
        genre: String,
        premise: String,
        structureTemplate: String,
        protagonistName: String,
        protagonistSecret: String,
        centralConflict: String,
        targetWordCount: Long = 45000L
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val colRef = db.collection("projects")
        val docRef = colRef.document()
        val validTemplate = if (structureTemplate in listOf("THREE_ACTS", "HERO_JOURNEY", "SNOWFLAKE")) {
            structureTemplate
        } else {
            "THREE_ACTS"
        }
        val payload = mapOf(
            "userId" to uid,
            "title" to title.trim().ifBlank { "Mi Primera Novela" }.take(150),
            "genre" to genre.trim().ifBlank { "Fantasía" }.take(60),
            "premise" to premise.trim().ifBlank { "Una historia sobre valentía y transformación." }.take(2000),
            "structureTemplate" to validTemplate,
            "protagonistName" to protagonistName.trim().ifBlank { "Elena" }.take(100),
            "protagonistSecret" to protagonistSecret.trim().ifBlank { "Guarda un recuerdo prohibido." }.take(500),
            "centralConflict" to centralConflict.trim().ifBlank { "Debe cruzar la frontera antes del eclipse." }.take(500),
            "targetWordCount" to targetWordCount.coerceIn(500L, 500000L),
            "currentWordCount" to 0L,
            "isSharedBeta" to false,
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

    suspend fun getProjectById(projectId: String): Result<StoryProject?> = runCatching {
        val docRef = db.collection("projects").document(projectId)
        try {
            val snap = docRef.get().await()
            snap.toObject(StoryProject::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun updateProjectWordCountAndBeta(
        projectId: String,
        currentWordCount: Long,
        isSharedBeta: Boolean
    ): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("projects").document(projectId)
        try {
            docRef.update(
                mapOf(
                    "currentWordCount" to currentWordCount.coerceIn(0L, 1000000L),
                    "isSharedBeta" to isSharedBeta,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteProject(projectId: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("projects").document(projectId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- CHARACTERS & LORE (BIBLE CARDS) ---

    fun observeCharacters(projectId: String, userId: String? = auth.currentUser?.uid): Flow<List<StoryCharacter>> = flow {
        val targetUid = userId ?: "unauthenticated"
        val path = "characters"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", targetUid)
                .whereEqualTo("projectId", projectId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(StoryCharacter::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun createCharacter(
        projectId: String,
        name: String,
        role: String,
        motivation: String,
        fear: String,
        secret: String,
        appearance: String,
        evolutionArc: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("characters").document()
        val validRole = if (role in listOf("PROTAGONISTA", "ANTAGONISTA", "MENTOR", "ALIADO", "MUNDO_LORE")) {
            role
        } else {
            "PROTAGONISTA"
        }
        val payload = mapOf(
            "userId" to uid,
            "projectId" to projectId,
            "name" to name.trim().ifBlank { "Personaje sin nombre" }.take(100),
            "role" to validRole,
            "motivation" to motivation.trim().ifBlank { "Descubrir la verdad." }.take(600),
            "fear" to fear.trim().ifBlank { "Perder a quienes protege." }.take(600),
            "secret" to secret.trim().ifBlank { "Oculta su verdadero origen." }.take(600),
            "appearance" to appearance.trim().ifBlank { "Mirada intensa y cicatriz antigua." }.take(600),
            "evolutionArc" to evolutionArc.trim().ifBlank { "De la duda a la determinación." }.take(600),
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

    suspend fun deleteCharacter(characterId: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("characters").document(characterId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- CHAPTERS (ZEN EDITOR) ---

    fun observeChapters(projectId: String, userId: String? = auth.currentUser?.uid): Flow<List<StoryChapter>> = flow {
        val targetUid = userId ?: "unauthenticated"
        val path = "chapters"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", targetUid)
                .whereEqualTo("projectId", projectId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(StoryChapter::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.chapterNumber }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun createChapter(
        projectId: String,
        chapterNumber: Long,
        actLabel: String,
        title: String,
        synopsis: String,
        content: String,
        status: String = "BORRADOR"
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("chapters").document()
        val cleanContent = content.take(100000)
        val words = countWords(cleanContent).toLong()
        val validStatus = if (status in listOf("BORRADOR", "EN_REVISION", "FINALIZADO")) status else "BORRADOR"
        val payload = mapOf(
            "userId" to uid,
            "projectId" to projectId,
            "chapterNumber" to chapterNumber.coerceIn(1L, 500L),
            "actLabel" to actLabel.trim().ifBlank { "Acto I" }.take(100),
            "title" to title.trim().ifBlank { "Capítulo $chapterNumber" }.take(150),
            "synopsis" to synopsis.trim().ifBlank { "Escena clave del capítulo." }.take(1000),
            "content" to cleanContent,
            "wordCount" to words,
            "status" to validStatus,
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

    suspend fun updateChapterContent(
        chapterId: String,
        title: String,
        synopsis: String,
        content: String,
        status: String
    ): Result<Long> = runCatching {
        requireUserId()
        val docRef = db.collection("chapters").document(chapterId)
        val cleanContent = content.take(100000)
        val words = countWords(cleanContent).toLong()
        val validStatus = if (status in listOf("BORRADOR", "EN_REVISION", "FINALIZADO")) status else "BORRADOR"
        try {
            docRef.update(
                mapOf(
                    "title" to title.trim().ifBlank { "Capítulo" }.take(150),
                    "synopsis" to synopsis.trim().ifBlank { "Escaleta del capítulo" }.take(1000),
                    "content" to cleanContent,
                    "wordCount" to words,
                    "status" to validStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            words
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- TIMELINE EVENTS ---

    fun observeTimelineEvents(projectId: String, userId: String? = auth.currentUser?.uid): Flow<List<TimelineEvent>> = flow {
        val targetUid = userId ?: "unauthenticated"
        val path = "timeline_events"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", targetUid)
                .whereEqualTo("projectId", projectId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(TimelineEvent::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.sequenceOrder }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun createTimelineEvent(
        projectId: String,
        sequenceOrder: Long,
        title: String,
        timeLabel: String,
        location: String,
        povCharacter: String,
        tensionLevel: Long,
        notes: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("timeline_events").document()
        val payload = mapOf(
            "userId" to uid,
            "projectId" to projectId,
            "sequenceOrder" to sequenceOrder.coerceIn(1L, 1000L),
            "title" to title.trim().ifBlank { "Evento narrativo" }.take(150),
            "timeLabel" to timeLabel.trim().ifBlank { "Día 1" }.take(100),
            "location" to location.trim().ifBlank { "Escenario principal" }.take(100),
            "povCharacter" to povCharacter.trim().ifBlank { "Protagonista" }.take(100),
            "tensionLevel" to tensionLevel.coerceIn(1L, 10L),
            "notes" to notes.trim().take(1000),
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

    suspend fun deleteTimelineEvent(eventId: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("timeline_events").document(eventId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- MICRO-LEARNING PROGRESS ---

    fun observeLessonProgress(userId: String? = auth.currentUser?.uid): Flow<List<LessonProgress>> = flow {
        val targetUid = userId ?: "unauthenticated"
        val path = "lesson_progress"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", targetUid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(LessonProgress::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveLessonPractice(
        lessonCode: String,
        practiceNote: String,
        completed: Boolean = true
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docId = "${uid}_${lessonCode.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")}".take(120)
        val docRef = db.collection("lesson_progress").document(docId)
        try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                docRef.update(
                    mapOf(
                        "completed" to completed,
                        "practiceNote" to practiceNote.trim().take(2000),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "lessonCode" to lessonCode.take(80),
                        "completed" to completed,
                        "practiceNote" to practiceNote.trim().take(2000),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    // --- SOCIAL BETA COMMENTS ---

    fun observeBetaComments(projectId: String, userId: String? = auth.currentUser?.uid): Flow<List<BetaComment>> = flow {
        val targetUid = userId ?: "unauthenticated"
        val path = "beta_comments"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", targetUid)
                .whereEqualTo("projectId", projectId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(BetaComment::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun addBetaComment(
        projectId: String,
        chapterTitle: String,
        reviewerName: String,
        paragraphExcerpt: String,
        commentText: String,
        rating: Long
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("beta_comments").document()
        val payload = mapOf(
            "userId" to uid,
            "projectId" to projectId,
            "chapterTitle" to chapterTitle.trim().ifBlank { "Capítulo 1" }.take(150),
            "reviewerName" to reviewerName.trim().ifBlank { "Lector Beta" }.take(100),
            "paragraphExcerpt" to paragraphExcerpt.trim().take(500),
            "commentText" to commentText.trim().ifBlank { "Excelente ritmo." }.take(1000),
            "rating" to rating.coerceIn(1L, 5L),
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

    companion object {
        fun countWords(text: String): Int {
            val trimmed = text.trim()
            if (trimmed.isEmpty()) return 0
            return trimmed.split(Regex("\\s+")).size
        }
    }
}
