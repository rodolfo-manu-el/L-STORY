package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.catalog.NarrativeCatalog
import com.example.data.model.StoryProject
import com.example.data.model.UserProfile

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WizardAndHomeScreen(
    userProfile: UserProfile?,
    projects: List<StoryProject>,
    selectedProjectId: String?,
    promptIdeas: List<String>,
    isAiLoading: Boolean,
    onSelectProject: (String) -> Unit,
    onLaunchWizardProject: (
        title: String,
        genre: String,
        protagonistName: String,
        protagonistMotivation: String,
        protagonistSecret: String,
        centralConflict: String,
        structureTemplate: String
    ) -> Unit,
    onGeneratePromptIdeas: (genre: String, seed: String) -> Unit,
    onRandomDramaticTrigger: () -> Unit,
    onOpenZenEditor: () -> Unit
) {
    var showWizardCard by remember { mutableStateOf(projects.isEmpty()) }
    var wizardStep by remember { mutableIntStateOf(0) }

    // Story Wizard state
    var title by remember { mutableStateOf("El Cartógrafo de Ceniza") }
    var genre by remember { mutableStateOf("Fantasía") }
    var protagonistName by remember { mutableStateOf("Lyra Vance") }
    var protagonistMotivation by remember { mutableStateOf("Encontrar la ciudad borrada del mapa antes del solsticio") }
    var protagonistSecret by remember { mutableStateOf("Fue ella quien quemó el archivo real por accidente") }
    var centralConflict by remember { mutableStateOf("El Inquisidor del Reloj persigue a todos los cartógrafos") }
    var selectedTemplate by remember { mutableStateOf("THREE_ACTS") }

    // Prompt Studio state
    var promptGenre by remember { mutableStateOf("Misterio / Thriller") }
    var promptSeed by remember { mutableStateOf("Una carta que llega veinte años tarde") }

    val genres = listOf("Fantasía", "Misterio / Thriller", "Ciencia Ficción", "Romance Histórico", "Realismo Mágico")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("wizard_home_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Banner + Daily Habit Streak Card (Módulo 2.4 Gamificación)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.img_story_hero),
                            contentDescription = "Estudio literario StoryBuilder",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Racha de escritura",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Racha: ${userProfile?.currentStreak ?: 1L} días",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    val todayWords = userProfile?.todayWords ?: 0L
                    val goalWords = (userProfile?.dailyWordGoal ?: 500L).coerceAtLeast(1L)
                    val progress = (todayWords.toFloat() / goalWords.toFloat()).coerceIn(0f, 1f)

                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Tu Co-Piloto contra la Hoja en Blanco",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Meta diaria: $todayWords / $goalWords palabras (${(progress * 100).toInt()}%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = { showWizardCard = !showWizardCard },
                                modifier = Modifier.testTag("toggle_story_wizard_button")
                            ) {
                                Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (showWizardCard) "Ocultar Wizard" else "Story Wizard")
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50))
                        )
                    }
                }
            }
        }

        // 2. Cuestionario Guiado de Arranque (Story Wizard - Módulo 1.1 & 1.4)
        item {
            AnimatedVisibility(visible = showWizardCard || projects.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("story_wizard_card")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "PASO ${wizardStep + 1} DE 3 · STORY WIZARD GUIADO",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                            Text(
                                text = "Sin bloqueo creativo",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Text(
                            text = "Construye los cimientos de tu novela respondiendo preguntas sencillas",
                            style = MaterialTheme.typography.titleLarge
                        )

                        when (wizardStep) {
                            0 -> {
                                Text(
                                    text = "Fase 1: Semilla e Identidad del Libro",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                OutlinedTextField(
                                    value = title,
                                    onValueChange = { title = it },
                                    label = { Text("Título provisional de tu historia") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("wizard_title_input")
                                )
                                Text(
                                    text = "Selecciona el género literario:",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    genres.forEach { g ->
                                        FilterChip(
                                            selected = genre == g,
                                            onClick = { genre = g },
                                            label = { Text(g) }
                                        )
                                    }
                                }
                            }
                            1 -> {
                                Text(
                                    text = "Fase 2: Protagonista, Secreto y Obstáculo",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                OutlinedTextField(
                                    value = protagonistName,
                                    onValueChange = { protagonistName = it },
                                    label = { Text("1. ¿Quién es tu protagonista?") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("wizard_protagonist_input")
                                )
                                OutlinedTextField(
                                    value = protagonistMotivation,
                                    onValueChange = { protagonistMotivation = it },
                                    label = { Text("2. ¿Qué desea conseguir desesperadamente?") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = protagonistSecret,
                                    onValueChange = { protagonistSecret = it },
                                    label = { Text("3. ¿Cuál es su gran secreto?") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = centralConflict,
                                    onValueChange = { centralConflict = it },
                                    label = { Text("4. ¿Qué obstáculo o antagonista se lo impide?") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            else -> {
                                Text(
                                    text = "Fase 3: Elige tu Plantilla de Estructura Narrativa",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                NarrativeCatalog.structureTemplates.forEach { template ->
                                    val isSelected = selectedTemplate == template.key
                                    Surface(
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedTemplate = template.key }
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = template.title,
                                                    style = MaterialTheme.typography.titleSmall
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = template.subtitle,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (wizardStep > 0) {
                                OutlinedButton(onClick = { wizardStep-- }) {
                                    Text("Anterior")
                                }
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }

                            if (wizardStep < 2) {
                                Button(
                                    onClick = { wizardStep++ },
                                    modifier = Modifier.testTag("wizard_next_button")
                                ) {
                                    Text("Siguiente paso")
                                }
                            } else {
                                Button(
                                    onClick = {
                                        onLaunchWizardProject(
                                            title,
                                            genre,
                                            protagonistName,
                                            protagonistMotivation,
                                            protagonistSecret,
                                            centralConflict,
                                            selectedTemplate
                                        )
                                        showWizardCard = false
                                        wizardStep = 0
                                    },
                                    modifier = Modifier.testTag("wizard_create_project_button")
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Generar Libro y Capítulo 1")
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Lista de Proyectos Activos
        item {
            Text(
                text = "Tus Proyectos Literarios (${projects.size})",
                style = MaterialTheme.typography.titleLarge
            )
        }

        if (projects.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Aún no tienes un manuscrito iniciado",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Completa los 3 pasos del Story Wizard arriba para generar automáticamente tu estructura, ficha de protagonista y primer capítulo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(projects, key = { it.id }) { project ->
                val isSelected = project.id == selectedProjectId
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectProject(project.id) }
                        .testTag("project_card_${project.id}")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = project.title,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = "${project.genre} · Plantilla: ${project.structureTemplate} · ${project.currentWordCount} palabras",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = {
                                    onSelectProject(project.id)
                                    onOpenZenEditor()
                                },
                                modifier = Modifier.testTag("open_editor_button_${project.id}")
                            ) {
                                Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Escribir")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = project.premise,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Protagonista: ${project.protagonistName} | Secreto: ${project.protagonistSecret}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        // 4. Generador de Ideas (Prompt Studio IA - Módulo 1.2)
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("prompt_studio_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Prompt Studio · Generador de Ideas e IA",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Detonantes dramáticos y semillas creativas para desbloquear nuevas tramas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            onClick = onRandomDramaticTrigger,
                            modifier = Modifier.testTag("random_trigger_button")
                        ) {
                            Icon(Icons.Default.Casino, contentDescription = "Azar", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Azar")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = promptGenre,
                            onValueChange = { promptGenre = it },
                            label = { Text("Género") },
                            modifier = Modifier.weight(0.4f)
                        )
                        OutlinedTextField(
                            value = promptSeed,
                            onValueChange = { promptSeed = it },
                            label = { Text("Idea o elemento disparador") },
                            modifier = Modifier.weight(0.6f)
                        )
                    }

                    Button(
                        onClick = { onGeneratePromptIdeas(promptGenre, promptSeed) },
                        enabled = !isAiLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_ai_prompts_button")
                    ) {
                        if (isAiLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Generar Semillas Narrativas con IA")
                    }

                    promptIdeas.forEachIndexed { index, idea ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Detonante #${index + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = idea,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = {
                                        centralConflict = idea.take(220)
                                        showWizardCard = true
                                        wizardStep = 1
                                    }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Usar en Story Wizard")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
