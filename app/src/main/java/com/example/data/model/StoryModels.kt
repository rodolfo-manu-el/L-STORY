package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "Autor Novel",
    val dailyWordGoal: Long = 500L,
    val currentStreak: Long = 1L,
    val todayWords: Long = 0L,
    val completedLessonsCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class StoryProject(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val genre: String = "Fantasía",
    val premise: String = "",
    val structureTemplate: String = "THREE_ACTS", // THREE_ACTS, HERO_JOURNEY, SNOWFLAKE
    val protagonistName: String = "",
    val protagonistSecret: String = "",
    val centralConflict: String = "",
    val targetWordCount: Long = 45000L,
    val currentWordCount: Long = 0L,
    val isSharedBeta: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class StoryCharacter(
    @DocumentId val id: String = "",
    val userId: String = "",
    val projectId: String = "",
    val name: String = "",
    val role: String = "PROTAGONISTA", // PROTAGONISTA, ANTAGONISTA, MENTOR, ALIADO, MUNDO_LORE
    val motivation: String = "",
    val fear: String = "",
    val secret: String = "",
    val appearance: String = "",
    val evolutionArc: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class StoryChapter(
    @DocumentId val id: String = "",
    val userId: String = "",
    val projectId: String = "",
    val chapterNumber: Long = 1L,
    val actLabel: String = "Acto I · Planteamiento",
    val title: String = "",
    val synopsis: String = "",
    val content: String = "",
    val wordCount: Long = 0L,
    val status: String = "BORRADOR", // BORRADOR, EN_REVISION, FINALIZADO
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class TimelineEvent(
    @DocumentId val id: String = "",
    val userId: String = "",
    val projectId: String = "",
    val sequenceOrder: Long = 1L,
    val title: String = "",
    val timeLabel: String = "Día 1 · Amanecer",
    val location: String = "",
    val povCharacter: String = "",
    val tensionLevel: Long = 5L,
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class LessonProgress(
    @DocumentId val id: String = "",
    val userId: String = "",
    val lessonCode: String = "",
    val completed: Boolean = false,
    val practiceNote: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class BetaComment(
    @DocumentId val id: String = "",
    val userId: String = "",
    val projectId: String = "",
    val chapterTitle: String = "",
    val reviewerName: String = "",
    val paragraphExcerpt: String = "",
    val commentText: String = "",
    val rating: Long = 5L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class MicroLesson(
    val code: String,
    val title: String,
    val category: String,
    val durationMinutes: Int,
    val summary: String,
    val goldenRule: String,
    val beforeExample: String,
    val afterExample: String,
    val interactivePrompt: String
)

data class NarrativeStructureOption(
    val key: String,
    val title: String,
    val subtitle: String,
    val defaultBeats: List<Pair<String, String>>
)

data class StyleIssue(
    val category: String,
    val severity: String, // ALTA, MEDIA, SUGERENCIA
    val excerpt: String,
    val explanation: String,
    val replacementSuggestion: String
)

data class BetaReaderReport(
    val overallScore: Int,
    val pacingVerdict: String,
    val tensionScore: Int,
    val dialogueVerdict: String,
    val sensoryAlternatives: List<Pair<String, String>>,
    val strengths: List<String>,
    val improvements: List<String>
)
