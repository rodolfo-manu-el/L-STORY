package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.StoryChapter
import com.example.data.model.StoryCharacter
import com.example.data.model.TimelineEvent
import com.example.data.repository.StoryRepository

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ZenEditorScreen(
    chapters: List<StoryChapter>,
    selectedChapterId: String?,
    characters: List<StoryCharacter>,
    timelineEvents: List<TimelineEvent>,
    synonymResults: Pair<List<String>, List<String>>?,
    isExpandedScreen: Boolean,
    onSelectChapter: (String) -> Unit,
    onCreateChapter: (title: String, actLabel: String, synopsis: String) -> Unit,
    onSaveChapter: (StoryChapter, String, String, String, String) -> Unit,
    onLookupSynonyms: (String) -> Unit
) {
    val activeChapter = chapters.find { it.id == selectedChapterId } ?: chapters.firstOrNull()

    var editorTitle by remember(activeChapter?.id) { mutableStateOf(activeChapter?.title ?: "Capítulo 1") }
    var editorSynopsis by remember(activeChapter?.id) { mutableStateOf(activeChapter?.synopsis ?: "") }
    var editorContent by remember(activeChapter?.id) { mutableStateOf(activeChapter?.content ?: "") }
    var editorStatus by remember(activeChapter?.id) { mutableStateOf(activeChapter?.status ?: "BORRADOR") }

    var focusMode by remember { mutableStateOf(false) }
    var showSplitReference by remember { mutableStateOf(isExpandedScreen) }
    var showLanguageAssistant by remember { mutableStateOf(false) }
    var synonymQuery by remember { mutableStateOf("miedo") }

    LaunchedEffect(activeChapter?.id) {
        if (activeChapter != null) {
            editorTitle = activeChapter.title
            editorSynopsis = activeChapter.synopsis
            editorContent = activeChapter.content
            editorStatus = activeChapter.status
        }
    }

    val liveWordCount = remember(editorContent) { StoryRepository.countWords(editorContent) }
    val activeParagraph = remember(editorContent) {
        editorContent.split("\n\n").lastOrNull { it.isNotBlank() }?.trim().orEmpty()
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Panel Principal del Editor Zen
        Column(
            modifier = Modifier
                .weight(if (showSplitReference && isExpandedScreen) 0.62f else 1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Selector de capítulos + barra de herramientas Zen
            AnimatedVisibility(visible = !focusMode) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Capítulos del Manuscrito",
                                style = MaterialTheme.typography.titleMedium
                            )
                            OutlinedButton(
                                onClick = {
                                    val nextNum = chapters.size + 1
                                    onCreateChapter(
                                        "Capítulo $nextNum: Nuevo Giro",
                                        "Acto II · Desarrollo",
                                        "Escena de complicación narrativa."
                                    )
                                },
                                modifier = Modifier.testTag("new_chapter_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Nuevo Capítulo")
                            }
                        }

                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            chapters.forEach { ch ->
                                FilterChip(
                                    selected = ch.id == activeChapter?.id,
                                    onClick = { onSelectChapter(ch.id) },
                                    label = { Text("Cap. ${ch.chapterNumber}: ${ch.title.take(18)}") }
                                )
                            }
                        }
                    }
                }
            }

            // Barra de Controles Zen (Modo Enfoque, Split Screen, Sinónimos, Guardar)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = focusMode,
                    onClick = { focusMode = !focusMode },
                    label = { Text(if (focusMode) "Salir Modo Zen" else "Modo Zen Enfoque") },
                    leadingIcon = {
                        Icon(Icons.Default.CenterFocusStrong, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier.testTag("zen_focus_toggle")
                )

                FilterChip(
                    selected = showSplitReference,
                    onClick = { showSplitReference = !showSplitReference },
                    label = { Text("Pantalla Dividida (Biblia)") },
                    leadingIcon = {
                        Icon(Icons.Default.VerticalSplit, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier.testTag("split_screen_toggle")
                )

                FilterChip(
                    selected = showLanguageAssistant,
                    onClick = { showLanguageAssistant = !showLanguageAssistant },
                    label = { Text("Sinónimos / Léxico") },
                    leadingIcon = {
                        Icon(Icons.Default.Spellcheck, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier.testTag("language_assistant_toggle")
                )

                if (activeChapter != null) {
                    Button(
                        onClick = {
                            onSaveChapter(
                                activeChapter,
                                editorTitle,
                                editorSynopsis,
                                editorContent,
                                editorStatus
                            )
                        },
                        modifier = Modifier.testTag("save_chapter_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Guardar ($liveWordCount pal.)")
                    }
                }
            }

            // Panel desplegable en móvil para Pantalla Dividida (Split Screen)
            AnimatedVisibility(visible = showSplitReference && !isExpandedScreen) {
                SplitReferencePanel(
                    characters = characters,
                    timelineEvents = timelineEvents,
                    synopsis = editorSynopsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Panel desplegable del Asistente de Lenguaje (Sinónimos y Antónimos)
            AnimatedVisibility(visible = showLanguageAssistant) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Asistente de Lenguaje · Sinónimos y Antónimos Literarios",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = synonymQuery,
                                onValueChange = { synonymQuery = it },
                                label = { Text("Palabra a enriquecer (ej. miedo, mirar, silencio)") },
                                modifier = Modifier.weight(1f)
                            )
                            Button(onClick = { onLookupSynonyms(synonymQuery) }) {
                                Text("Buscar")
                            }
                        }
                        if (synonymResults != null) {
                            Text(
                                text = "Sinónimos sugeridos (toca para insertar):",
                                style = MaterialTheme.typography.labelMedium
                            )
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                synonymResults.first.forEach { syn ->
                                    AssistChip(
                                        onClick = { editorContent = "$editorContent $syn" },
                                        label = { Text(syn) }
                                    )
                                }
                            }
                            Text(
                                text = "Antónimos de contraste: ${synonymResults.second.joinToString(", ")}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Lienzo Editorial Zen
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!focusMode) {
                        OutlinedTextField(
                            value = editorTitle,
                            onValueChange = { editorTitle = it },
                            label = { Text("Título del Capítulo") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editorSynopsis,
                            onValueChange = { editorSynopsis = it },
                            label = { Text("Escaleta / Objetivo dramático de la escena") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Barra rápida de formato Markdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = { editorContent += "\n\n— " },
                            label = { Text("— Diálogo") }
                        )
                        AssistChip(
                            onClick = { editorContent += " **énfasis** " },
                            label = { Text("**Negrita**") }
                        )
                        AssistChip(
                            onClick = { editorContent += " *pensamiento* " },
                            label = { Text("*Cursiva*") }
                        )
                        AssistChip(
                            onClick = { editorContent += "\n\n* * *\n\n" },
                            label = { Text("* * * Corte") }
                        )
                    }

                    // Foco de Párrafo Activo cuando Modo Zen está activo
                    AnimatedVisibility(visible = focusMode && activeParagraph.isNotEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "ENFOQUE EN PÁRRAFO ACTIVO",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = activeParagraph,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = editorContent,
                        onValueChange = { editorContent = it },
                        label = { Text("Manuscrito en tipografía literaria Lora (Soporta Markdown)") },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        minLines = 12,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("zen_editor_textfield")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$liveWordCount palabras · ${(liveWordCount / 200).coerceAtLeast(1)} min de lectura",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("BORRADOR", "EN_REVISION", "FINALIZADO").forEach { st ->
                                FilterChip(
                                    selected = editorStatus == st,
                                    onClick = { editorStatus = st },
                                    label = { Text(st) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Panel Lateral en Pantallas Grandes (Split Screen Canónico)
        if (showSplitReference && isExpandedScreen) {
            SplitReferencePanel(
                characters = characters,
                timelineEvents = timelineEvents,
                synopsis = editorSynopsis,
                modifier = Modifier
                    .weight(0.38f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun SplitReferencePanel(
    characters: List<StoryCharacter>,
    timelineEvents: List<TimelineEvent>,
    synopsis: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier.testTag("split_reference_panel")
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Consulta Rápida (Split View)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (synopsis.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "ESCALETA DEL CAPÍTULO",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(text = synopsis, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Text(
                text = "Fichas de Personajes (${characters.size})",
                style = MaterialTheme.typography.labelMedium
            )
            characters.forEach { ch ->
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "${ch.name} (${ch.role})",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Motivación: ${ch.motivation}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Secreto: ${ch.secret}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            if (timelineEvents.isNotEmpty()) {
                Text(
                    text = "Línea de Tiempo (${timelineEvents.size})",
                    style = MaterialTheme.typography.labelMedium
                )
                timelineEvents.forEach { ev ->
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "#${ev.sequenceOrder} ${ev.title} (${ev.timeLabel})",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "POV: ${ev.povCharacter} · Tensión ${ev.tensionLevel}/10",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
