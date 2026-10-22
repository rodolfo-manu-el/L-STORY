package com.example.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.NarrativeAiService
import com.example.data.catalog.NarrativeCatalog
import com.example.data.export.ExportFormat
import com.example.data.export.ExportResult
import com.example.data.export.ManuscriptExporter
import com.example.data.model.BetaComment
import com.example.data.model.BetaReaderReport
import com.example.data.model.LessonProgress
import com.example.data.model.StoryChapter
import com.example.data.model.StoryCharacter
import com.example.data.model.StoryProject
import com.example.data.model.StyleIssue
import com.example.data.model.TimelineEvent
import com.example.data.model.UserProfile
import com.example.data.repository.StoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

@OptIn(ExperimentalCoroutinesApi::class)
class StoryViewModel(
    private val repository: StoryRepository,
    private val currentUserId: String,
    private val userDisplayName: String
) : ViewModel() {

    private val _selectedProjectId = MutableStateFlow<String?>(null)
    val selectedProjectId: StateFlow<String?> = _selectedProjectId.asStateFlow()

    private val _selectedChapterId = MutableStateFlow<String?>(null)
    val selectedChapterId: StateFlow<String?> = _selectedChapterId.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    private val _promptIdeas = MutableStateFlow<List<String>>(NarrativeCatalog.dramaticTriggers.take(3))
    val promptIdeas: StateFlow<List<String>> = _promptIdeas.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _showDontTellOptions = MutableStateFlow<List<String>>(emptyList())
    val showDontTellOptions: StateFlow<List<String>> = _showDontTellOptions.asStateFlow()

    private val _styleIssues = MutableStateFlow<List<StyleIssue>>(emptyList())
    val styleIssues: StateFlow<List<StyleIssue>> = _styleIssues.asStateFlow()

    private val _betaReport = MutableStateFlow<BetaReaderReport?>(null)
    val betaReport: StateFlow<BetaReaderReport?> = _betaReport.asStateFlow()

    private val _synonymResults = MutableStateFlow<Pair<List<String>, List<String>>?>(null)
    val synonymResults: StateFlow<Pair<List<String>, List<String>>?> = _synonymResults.asStateFlow()

    private val _lastExportResult = MutableStateFlow<ExportResult?>(null)
    val lastExportResult: StateFlow<ExportResult?> = _lastExportResult.asStateFlow()

    val userProfileState: StateFlow<UiState<UserProfile?>> = repository.observeUserProfile(currentUserId)
        .map<UserProfile?, UiState<UserProfile?>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing profile", e)
            emit(UiState.Error(e.message ?: "Error al cargar el perfil"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val projectsState: StateFlow<UiState<List<StoryProject>>> = repository.observeProjects(currentUserId)
        .map<List<StoryProject>, UiState<List<StoryProject>>> { list ->
            if (_selectedProjectId.value == null && list.isNotEmpty()) {
                _selectedProjectId.value = list.first().id
            }
            UiState.Success(list)
        }
        .catch { e ->
            Log.w(TAG, "Error observing projects", e)
            emit(UiState.Error(e.message ?: "Error al cargar proyectos"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val charactersState: StateFlow<UiState<List<StoryCharacter>>> = _selectedProjectId
        .flatMapLatest { projId ->
            if (projId == null) flowOf(emptyList())
            else repository.observeCharacters(projId, currentUserId)
        }
        .map<List<StoryCharacter>, UiState<List<StoryCharacter>>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing characters", e)
            emit(UiState.Error(e.message ?: "Error al cargar fichas de personajes"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val chaptersState: StateFlow<UiState<List<StoryChapter>>> = _selectedProjectId
        .flatMapLatest { projId ->
            if (projId == null) flowOf(emptyList())
            else repository.observeChapters(projId, currentUserId)
        }
        .map<List<StoryChapter>, UiState<List<StoryChapter>>> { list ->
            if (list.isNotEmpty() && (_selectedChapterId.value == null || list.none { it.id == _selectedChapterId.value })) {
                _selectedChapterId.value = list.first().id
            }
            UiState.Success(list)
        }
        .catch { e ->
            Log.w(TAG, "Error observing chapters", e)
            emit(UiState.Error(e.message ?: "Error al cargar capítulos"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val timelineState: StateFlow<UiState<List<TimelineEvent>>> = _selectedProjectId
        .flatMapLatest { projId ->
            if (projId == null) flowOf(emptyList())
            else repository.observeTimelineEvents(projId, currentUserId)
        }
        .map<List<TimelineEvent>, UiState<List<TimelineEvent>>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing timeline", e)
            emit(UiState.Error(e.message ?: "Error al cargar línea de tiempo"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val lessonProgressState: StateFlow<UiState<List<LessonProgress>>> = repository.observeLessonProgress(currentUserId)
        .map<List<LessonProgress>, UiState<List<LessonProgress>>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing lessons", e)
            emit(UiState.Error(e.message ?: "Error al cargar progreso de lecciones"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val betaCommentsState: StateFlow<UiState<List<BetaComment>>> = _selectedProjectId
        .flatMapLatest { projId ->
            if (projId == null) flowOf(emptyList())
            else repository.observeBetaComments(projId, currentUserId)
        }
        .map<List<BetaComment>, UiState<List<BetaComment>>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing beta comments", e)
            emit(UiState.Error(e.message ?: "Error al cargar comentarios beta"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    init {
        viewModelScope.launch {
            repository.ensureUserProfile(userDisplayName)
        }
    }

    fun dismissBanner() {
        _statusBanner.value = null
    }

    fun selectProject(projectId: String) {
        _selectedProjectId.value = projectId
        _selectedChapterId.value = null
    }

    fun selectChapter(chapterId: String) {
        _selectedChapterId.value = chapterId
    }

    /**
     * Story Wizard (Módulo 1): Crea el proyecto completo + Ficha del Protagonista +
     * Escaleta de Capítulos según la plantilla elegida + Hitos iniciales de Cronología.
     */
    fun launchGuidedStoryWizard(
        title: String,
        genre: String,
        protagonistName: String,
        protagonistMotivation: String,
        protagonistSecret: String,
        centralConflict: String,
        structureTemplate: String,
        targetWordCount: Long = 45000L
    ) {
        viewModelScope.launch {
            val premise = "En un mundo de $genre, $protagonistName busca $protagonistMotivation mientras oculta que $protagonistSecret, enfrentándose a $centralConflict."
            val projectResult = repository.createProject(
                title = title,
                genre = genre,
                premise = premise,
                structureTemplate = structureTemplate,
                protagonistName = protagonistName,
                protagonistSecret = protagonistSecret,
                centralConflict = centralConflict,
                targetWordCount = targetWordCount
            )
            projectResult.onSuccess { projectId ->
                _selectedProjectId.value = projectId

                // 1. Crear Ficha del Protagonista (Bible Card)
                repository.createCharacter(
                    projectId = projectId,
                    name = protagonistName,
                    role = "PROTAGONISTA",
                    motivation = protagonistMotivation,
                    fear = "Que su secreto ($protagonistSecret) destruya aquello que intenta salvar.",
                    secret = protagonistSecret,
                    appearance = "Rasgos marcados por la experiencia; porta un objeto ligado a su pasado.",
                    evolutionArc = "Pasa de huir de su secreto a aceptarlo para superar $centralConflict."
                )

                // 2. Crear Capítulos iniciales basados en la plantilla seleccionada
                val template = NarrativeCatalog.structureTemplates.find { it.key == structureTemplate }
                    ?: NarrativeCatalog.structureTemplates.first()

                val firstChapterContent = buildString {
                    appendLine("# Capítulo 1: El Umbral de $protagonistName")
                    appendLine()
                    appendLine("Nadie en aquel lugar sospechaba que $protagonistName guardaba un secreto capaz de cambiarlo todo: $protagonistSecret.")
                    appendLine()
                    appendLine("Su único objetivo era claro: $protagonistMotivation. Sin embargo, la sombra de $centralConflict ya había comenzado a cerrarse sobre su camino.")
                    appendLine()
                    appendLine("—Si damos un paso más, no habrá retorno —murmuró mirando el horizonte.")
                }

                template.defaultBeats.forEachIndexed { index, (actTitle, beatSynopsis) ->
                    val chNum = (index + 1).toLong()
                    repository.createChapter(
                        projectId = projectId,
                        chapterNumber = chNum,
                        actLabel = actTitle,
                        title = if (index == 0) "El Umbral de $protagonistName" else actTitle.substringAfter("·").trim(),
                        synopsis = beatSynopsis,
                        content = if (index == 0) firstChapterContent else "",
                        status = if (index == 0) "EN_REVISION" else "BORRADOR"
                    )
                }

                // 3. Crear Eventos iniciales en la Cronología Visual
                repository.createTimelineEvent(
                    projectId = projectId,
                    sequenceOrder = 1L,
                    title = "Incidente Incitador: $centralConflict",
                    timeLabel = "Día 1 · Amanecer",
                    location = "Escenario de apertura",
                    povCharacter = protagonistName,
                    tensionLevel = 6L,
                    notes = "El secreto de $protagonistName corre peligro por primera vez."
                )
                repository.createTimelineEvent(
                    projectId = projectId,
                    sequenceOrder = 2L,
                    title = "Punto de No Retorno",
                    timeLabel = "Día 3 · Medianoche",
                    location = "Frontera del conflicto",
                    povCharacter = protagonistName,
                    tensionLevel = 8L,
                    notes = "Revelación parcial de: $protagonistSecret."
                )

                val initialWords = StoryRepository.countWords(firstChapterContent).toLong()
                repository.updateProjectWordCountAndBeta(projectId, initialWords, false)
                _statusBanner.value = "¡Proyecto \"$title\" estructurado con éxito! Tu primer capítulo ya está listo en el Editor Zen."
            }.onFailure { err ->
                _statusBanner.value = "Error al crear proyecto: ${err.localizedMessage}"
            }
        }
    }

    fun addCharacterCard(
        name: String,
        role: String,
        motivation: String,
        fear: String,
        secret: String,
        appearance: String,
        evolutionArc: String
    ) {
        val projId = _selectedProjectId.value ?: return
        viewModelScope.launch {
            repository.createCharacter(
                projectId = projId,
                name = name,
                role = role,
                motivation = motivation,
                fear = fear,
                secret = secret,
                appearance = appearance,
                evolutionArc = evolutionArc
            ).onSuccess {
                _statusBanner.value = "Ficha de \"$name\" añadida a la Biblia de la Historia."
            }
        }
    }

    fun removeCharacter(characterId: String) {
        viewModelScope.launch {
            repository.deleteCharacter(characterId)
        }
    }

    fun addTimelineEvent(
        sequenceOrder: Long,
        title: String,
        timeLabel: String,
        location: String,
        povCharacter: String,
        tensionLevel: Long,
        notes: String
    ) {
        val projId = _selectedProjectId.value ?: return
        viewModelScope.launch {
            repository.createTimelineEvent(
                projectId = projId,
                sequenceOrder = sequenceOrder,
                title = title,
                timeLabel = timeLabel,
                location = location,
                povCharacter = povCharacter,
                tensionLevel = tensionLevel,
                notes = notes
            ).onSuccess {
                _statusBanner.value = "Evento \"$title\" añadido a la Cronología Visual."
            }
        }
    }

    fun removeTimelineEvent(eventId: String) {
        viewModelScope.launch {
            repository.deleteTimelineEvent(eventId)
        }
    }

    fun addNewChapter(title: String, actLabel: String, synopsis: String) {
        val projId = _selectedProjectId.value ?: return
        val existingChapters = (chaptersState.value as? UiState.Success)?.data.orEmpty()
        val nextNumber = (existingChapters.maxOfOrNull { it.chapterNumber } ?: 0L) + 1L
        viewModelScope.launch {
            repository.createChapter(
                projectId = projId,
                chapterNumber = nextNumber,
                actLabel = actLabel,
                title = title,
                synopsis = synopsis,
                content = "",
                status = "BORRADOR"
            ).onSuccess { newId ->
                _selectedChapterId.value = newId
                _statusBanner.value = "Capítulo $nextNumber creado."
            }
        }
    }

    fun saveChapterDraft(
        chapter: StoryChapter,
        newTitle: String,
        newSynopsis: String,
        newContent: String,
        newStatus: String
    ) {
        val projId = _selectedProjectId.value ?: return
        viewModelScope.launch {
            repository.updateChapterContent(
                chapterId = chapter.id,
                title = newTitle,
                synopsis = newSynopsis,
                content = newContent,
                status = newStatus
            ).onSuccess { updatedWords ->
                val allChapters = (chaptersState.value as? UiState.Success)?.data.orEmpty()
                val totalWords = allChapters.sumOf {
                    if (it.id == chapter.id) updatedWords else it.wordCount
                }
                val currentProj = (projectsState.value as? UiState.Success)?.data?.find { it.id == projId }
                repository.updateProjectWordCountAndBeta(
                    projectId = projId,
                    currentWordCount = totalWords,
                    isSharedBeta = currentProj?.isSharedBeta ?: false
                )

                val profile = (userProfileState.value as? UiState.Success)?.data
                val delta = (updatedWords - chapter.wordCount).coerceAtLeast(10L)
                repository.updateWritingGoalAndProgress(
                    dailyWordGoal = profile?.dailyWordGoal ?: 500L,
                    todayWords = (profile?.todayWords ?: 0L) + delta,
                    currentStreak = (profile?.currentStreak ?: 1L).coerceAtLeast(1L),
                    completedLessonsCount = profile?.completedLessonsCount ?: 0L
                )
                _statusBanner.value = "Guardado en la nube · $updatedWords palabras en este capítulo."
            }
        }
    }

    fun updateDailyGoal(newGoal: Long) {
        val profile = (userProfileState.value as? UiState.Success)?.data
        viewModelScope.launch {
            repository.updateWritingGoalAndProgress(
                dailyWordGoal = newGoal,
                todayWords = profile?.todayWords ?: 0L,
                currentStreak = profile?.currentStreak ?: 1L,
                completedLessonsCount = profile?.completedLessonsCount ?: 0L
            )
            _statusBanner.value = "Meta diaria actualizada a $newGoal palabras."
        }
    }

    // --- IA & CO-PILOTO NARRATIVO ---

    fun generatePromptStudioIdeas(genre: String, seed: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _promptIdeas.value = NarrativeAiService.generateStoryIdeas(genre, seed)
            _isAiLoading.value = false
        }
    }

    fun pickRandomDramaticTrigger() {
        _promptIdeas.value = NarrativeCatalog.dramaticTriggers.shuffled().take(3)
    }

    fun runCoherenceAndStyleAudit(content: String) {
        _styleIssues.value = NarrativeAiService.analyzeCoherenceAndStyleLocal(content)
    }

    fun runShowDontTellGenerator(flatSentence: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _showDontTellOptions.value = NarrativeAiService.transformShowDontTell(flatSentence)
            _isAiLoading.value = false
        }
    }

    fun runVirtualBetaSimulation(chapterTitle: String, content: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _betaReport.value = NarrativeAiService.runVirtualBetaReader(chapterTitle, content)
            _isAiLoading.value = false
        }
    }

    fun lookupWordSynonyms(word: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _synonymResults.value = NarrativeAiService.lookupSynonymsAndGrammar(word)
            _isAiLoading.value = false
        }
    }

    // --- MICRO-APRENDIZAJE, BETA SOCIAL Y EXPORTACIÓN ---

    fun completeMicroLesson(lessonCode: String, practiceNote: String) {
        viewModelScope.launch {
            repository.saveLessonPractice(
                lessonCode = lessonCode,
                practiceNote = practiceNote,
                completed = true
            ).onSuccess {
                val completedList = (lessonProgressState.value as? UiState.Success)?.data.orEmpty()
                val newCount = (completedList.map { it.lessonCode } + lessonCode).distinct().size.toLong()
                val profile = (userProfileState.value as? UiState.Success)?.data
                repository.updateWritingGoalAndProgress(
                    dailyWordGoal = profile?.dailyWordGoal ?: 500L,
                    todayWords = profile?.todayWords ?: 0L,
                    currentStreak = profile?.currentStreak ?: 1L,
                    completedLessonsCount = newCount
                )
                _statusBanner.value = "¡Píldora narrativa completada y ejercicio guardado!"
            }
        }
    }

    fun toggleSocialBetaShare(enabled: Boolean) {
        val projId = _selectedProjectId.value ?: return
        val currentProj = (projectsState.value as? UiState.Success)?.data?.find { it.id == projId } ?: return
        viewModelScope.launch {
            repository.updateProjectWordCountAndBeta(
                projectId = projId,
                currentWordCount = currentProj.currentWordCount,
                isSharedBeta = enabled
            )
            _statusBanner.value = if (enabled) {
                "Enlace de Lector Beta Social activado para recibir notas al margen."
            } else {
                "Borrador cambiado a modo privado."
            }
        }
    }

    fun addMarginBetaComment(
        chapterTitle: String,
        reviewerName: String,
        excerpt: String,
        commentText: String,
        rating: Long
    ) {
        val projId = _selectedProjectId.value ?: return
        viewModelScope.launch {
            repository.addBetaComment(
                projectId = projId,
                chapterTitle = chapterTitle,
                reviewerName = reviewerName,
                paragraphExcerpt = excerpt,
                commentText = commentText,
                rating = rating
            ).onSuccess {
                _statusBanner.value = "Nota al margen de $reviewerName añadida al capítulo."
            }
        }
    }

    fun exportCurrentManuscript(context: Context, format: ExportFormat) {
        val projId = _selectedProjectId.value ?: return
        val project = (projectsState.value as? UiState.Success)?.data?.find { it.id == projId } ?: return
        val chapters = (chaptersState.value as? UiState.Success)?.data.orEmpty()
        val characters = (charactersState.value as? UiState.Success)?.data.orEmpty()

        ManuscriptExporter.exportProject(
            context = context,
            project = project,
            chapters = chapters,
            characters = characters,
            format = format
        ).onSuccess { result ->
            _lastExportResult.value = result
            _statusBanner.value = result.summaryMessage
        }.onFailure { err ->
            _statusBanner.value = "Error al exportar: ${err.localizedMessage}"
        }
    }

    companion object {
        private const val TAG = "StoryViewModel"
    }
}
