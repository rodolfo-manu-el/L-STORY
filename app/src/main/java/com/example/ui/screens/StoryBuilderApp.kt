package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.data.repository.StoryRepository
import com.example.ui.auth.signOut
import com.example.ui.viewmodel.StoryViewModel
import com.example.ui.viewmodel.UiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private data class NavDest(
    val index: Int,
    val label: String,
    val icon: ImageVector,
    val tag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryBuilderApp(
    currentUserId: String,
    userDisplayName: String,
    onSignedOut: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: StoryViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                StoryViewModel(
                    repository = StoryRepository(db, FirebaseAuth.getInstance()),
                    currentUserId = currentUserId,
                    userDisplayName = userDisplayName
                )
            }
        }
    )

    val userProfileState by viewModel.userProfileState.collectAsStateWithLifecycle()
    val projectsState by viewModel.projectsState.collectAsStateWithLifecycle()
    val charactersState by viewModel.charactersState.collectAsStateWithLifecycle()
    val chaptersState by viewModel.chaptersState.collectAsStateWithLifecycle()
    val timelineState by viewModel.timelineState.collectAsStateWithLifecycle()
    val lessonProgressState by viewModel.lessonProgressState.collectAsStateWithLifecycle()
    val betaCommentsState by viewModel.betaCommentsState.collectAsStateWithLifecycle()

    val selectedProjectId by viewModel.selectedProjectId.collectAsStateWithLifecycle()
    val selectedChapterId by viewModel.selectedChapterId.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()
    val promptIdeas by viewModel.promptIdeas.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val showDontTellOptions by viewModel.showDontTellOptions.collectAsStateWithLifecycle()
    val styleIssues by viewModel.styleIssues.collectAsStateWithLifecycle()
    val betaReport by viewModel.betaReport.collectAsStateWithLifecycle()
    val synonymResults by viewModel.synonymResults.collectAsStateWithLifecycle()
    val lastExportResult by viewModel.lastExportResult.collectAsStateWithLifecycle()

    val userProfile = (userProfileState as? UiState.Success)?.data
    val projects = (projectsState as? UiState.Success)?.data.orEmpty()
    val characters = (charactersState as? UiState.Success)?.data.orEmpty()
    val chapters = (chaptersState as? UiState.Success)?.data.orEmpty()
    val timelineEvents = (timelineState as? UiState.Success)?.data.orEmpty()
    val lessonProgress = (lessonProgressState as? UiState.Success)?.data.orEmpty()
    val betaComments = (betaCommentsState as? UiState.Success)?.data.orEmpty()

    val activeProject = projects.find { it.id == selectedProjectId } ?: projects.firstOrNull()
    val activeChapter = chapters.find { it.id == selectedChapterId } ?: chapters.firstOrNull()

    var currentTab by rememberSaveable { mutableIntStateOf(0) }

    if (currentTab != 0) {
        BackHandler {
            currentTab = 0
        }
    }

    val destinations = remember {
        listOf(
            NavDest(0, "Wizard", Icons.Default.AutoFixHigh, "nav_wizard"),
            NavDest(1, "Biblia", Icons.Default.Groups, "nav_bible"),
            NavDest(2, "Editor Zen", Icons.Default.EditNote, "nav_editor"),
            NavDest(3, "IA Co-Piloto", Icons.Default.AutoAwesome, "nav_copilot"),
            NavDest(4, "Academia", Icons.Default.School, "nav_academy")
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpandedScreen = maxWidth >= 700.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = activeProject?.title ?: "StoryBuilder · Estudio Narrativo",
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (activeProject != null) {
                                    "${activeProject.genre} · ${activeProject.currentWordCount}/${activeProject.targetWordCount} palabras"
                                } else {
                                    "Autor: $userDisplayName · Co-Piloto Guiado"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                signOut(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onSignOutComplete = onSignedOut,
                                    scope = scope
                                )
                            },
                            modifier = Modifier.testTag("sign_out_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Cerrar sesión"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                if (!isExpandedScreen) {
                    NavigationBar {
                        destinations.forEach { dest ->
                            NavigationBarItem(
                                selected = currentTab == dest.index,
                                onClick = { currentTab = dest.index },
                                icon = { Icon(dest.icon, contentDescription = dest.label) },
                                label = { Text(dest.label, maxLines = 1) },
                                modifier = Modifier.testTag(dest.tag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isExpandedScreen) {
                    NavigationRail {
                        destinations.forEach { dest ->
                            NavigationRailItem(
                                selected = currentTab == dest.index,
                                onClick = { currentTab = dest.index },
                                icon = { Icon(dest.icon, contentDescription = dest.label) },
                                label = { Text(dest.label) },
                                modifier = Modifier.testTag(dest.tag)
                            )
                        }
                    }
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    AnimatedVisibility(visible = statusBanner != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = statusBanner.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(onClick = { viewModel.dismissBanner() }) {
                                    Text("OK")
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        when (currentTab) {
                            0 -> WizardAndHomeScreen(
                                userProfile = userProfile,
                                projects = projects,
                                selectedProjectId = activeProject?.id,
                                promptIdeas = promptIdeas,
                                isAiLoading = isAiLoading,
                                onSelectProject = { viewModel.selectProject(it) },
                                onLaunchWizardProject = { title, genre, protName, protMot, protSec, conflict, template ->
                                    viewModel.launchGuidedStoryWizard(
                                        title = title,
                                        genre = genre,
                                        protagonistName = protName,
                                        protagonistMotivation = protMot,
                                        protagonistSecret = protSec,
                                        centralConflict = conflict,
                                        structureTemplate = template
                                    )
                                },
                                onGeneratePromptIdeas = { g, s -> viewModel.generatePromptStudioIdeas(g, s) },
                                onRandomDramaticTrigger = { viewModel.pickRandomDramaticTrigger() },
                                onOpenZenEditor = { currentTab = 2 }
                            )

                            1 -> BibleAndTimelineScreen(
                                characters = characters,
                                timelineEvents = timelineEvents,
                                onAddCharacter = { name, role, mot, fear, sec, app, arc ->
                                    viewModel.addCharacterCard(name, role, mot, fear, sec, app, arc)
                                },
                                onDeleteCharacter = { viewModel.removeCharacter(it) },
                                onAddTimelineEvent = { order, title, time, loc, pov, tension, notes ->
                                    viewModel.addTimelineEvent(order, title, time, loc, pov, tension, notes)
                                },
                                onDeleteTimelineEvent = { viewModel.removeTimelineEvent(it) }
                            )

                            2 -> ZenEditorScreen(
                                chapters = chapters,
                                selectedChapterId = activeChapter?.id,
                                characters = characters,
                                timelineEvents = timelineEvents,
                                synonymResults = synonymResults,
                                isExpandedScreen = isExpandedScreen,
                                onSelectChapter = { viewModel.selectChapter(it) },
                                onCreateChapter = { title, act, syn -> viewModel.addNewChapter(title, act, syn) },
                                onSaveChapter = { ch, t, s, c, st -> viewModel.saveChapterDraft(ch, t, s, c, st) },
                                onLookupSynonyms = { viewModel.lookupWordSynonyms(it) }
                            )

                            3 -> AiCopilotScreen(
                                activeChapter = activeChapter,
                                isAiLoading = isAiLoading,
                                styleIssues = styleIssues,
                                showDontTellOptions = showDontTellOptions,
                                betaReport = betaReport,
                                onRunCoherenceAudit = { viewModel.runCoherenceAndStyleAudit(it) },
                                onRunShowDontTell = { viewModel.runShowDontTellGenerator(it) },
                                onRunVirtualBeta = { title, text -> viewModel.runVirtualBetaSimulation(title, text) },
                                onAppendToActiveChapter = { sensoryLine ->
                                    if (activeChapter != null) {
                                        viewModel.saveChapterDraft(
                                            chapter = activeChapter,
                                            newTitle = activeChapter.title,
                                            newSynopsis = activeChapter.synopsis,
                                            newContent = "${activeChapter.content}\n\n$sensoryLine",
                                            newStatus = activeChapter.status
                                        )
                                    }
                                }
                            )

                            4 -> AcademyAndExportScreen(
                                activeProject = activeProject,
                                lessonProgress = lessonProgress,
                                betaComments = betaComments,
                                lastExportResult = lastExportResult,
                                onCompleteLesson = { code, note -> viewModel.completeMicroLesson(code, note) },
                                onToggleBetaShare = { viewModel.toggleSocialBetaShare(it) },
                                onAddBetaComment = { chTitle, revName, exc, comm, rate ->
                                    viewModel.addMarginBetaComment(chTitle, revName, exc, comm, rate)
                                },
                                onExportFormat = { fmt -> viewModel.exportCurrentManuscript(context, fmt) }
                            )
                        }
                    }
                }
            }
        }
    }
}
