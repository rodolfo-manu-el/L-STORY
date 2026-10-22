package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.catalog.NarrativeCatalog
import com.example.data.export.ExportFormat
import com.example.data.export.ExportResult
import com.example.data.export.ManuscriptExporter
import com.example.data.model.BetaComment
import com.example.data.model.LessonProgress
import com.example.data.model.StoryProject

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AcademyAndExportScreen(
    activeProject: StoryProject?,
    lessonProgress: List<LessonProgress>,
    betaComments: List<BetaComment>,
    lastExportResult: ExportResult?,
    onCompleteLesson: (lessonCode: String, practiceNote: String) -> Unit,
    onToggleBetaShare: (Boolean) -> Unit,
    onAddBetaComment: (
        chapterTitle: String,
        reviewerName: String,
        excerpt: String,
        commentText: String,
        rating: Long
    ) -> Unit,
    onExportFormat: (ExportFormat) -> Unit
) {
    val context = LocalContext.current
    var selectedSection by remember { mutableIntStateOf(0) }

    // Beta Social state
    var reviewerName by remember { mutableStateOf("Sofía (Lectora Beta)") }
    var excerpt by remember { mutableStateOf("«Si damos un paso más, no habrá retorno»") }
    var commentText by remember { mutableStateOf("¡Me encantó este cierre! Transmite urgencia y misterio.") }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedSection) {
            Tab(
                selected = selectedSection == 0,
                onClick = { selectedSection = 0 },
                text = { Text("Píldoras (2m)") },
                icon = { Icon(Icons.Default.School, contentDescription = null) },
                modifier = Modifier.testTag("tab_micro_lessons")
            )
            Tab(
                selected = selectedSection == 1,
                onClick = { selectedSection = 1 },
                text = { Text("Beta & Exportar") },
                icon = { Icon(Icons.Default.Download, contentDescription = null) },
                modifier = Modifier.testTag("tab_export_and_beta")
            )
            Tab(
                selected = selectedSection == 2,
                onClick = { selectedSection = 2 },
                text = { Text("Arquitectura") },
                icon = { Icon(Icons.Default.Architecture, contentDescription = null) },
                modifier = Modifier.testTag("tab_tech_spec")
            )
        }

        when (selectedSection) {
            0 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text(
                            text = "Píldoras Narrativas · Micro-Lecciones de 2 Minutos",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Aprende técnicas literarias mientras escribes tu libro, sin teoría pesada.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    items(NarrativeCatalog.microLessons, key = { it.code }) { lesson ->
                        val isDone = lessonProgress.any { it.lessonCode == lesson.code && it.completed }
                        var practiceInput by remember(lesson.code) {
                            mutableStateOf(
                                lessonProgress.find { it.lessonCode == lesson.code }?.practiceNote.orEmpty()
                            )
                        }

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDone) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = "${lesson.category.uppercase()} · ${lesson.durationMinutes} MIN",
                                            style = MaterialTheme.typography.labelMedium,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                    if (isDone) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Completada", style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                }

                                Text(lesson.title, style = MaterialTheme.typography.titleLarge)
                                Text(lesson.summary, style = MaterialTheme.typography.bodyMedium)

                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "Regla de Oro: ${lesson.goldenRule}",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "✗ Antes: ${lesson.beforeExample}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "✓ Después: ${lesson.afterExample}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = practiceInput,
                                    onValueChange = { practiceInput = it },
                                    label = { Text("Práctica: ${lesson.interactivePrompt}") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        onCompleteLesson(
                                            lesson.code,
                                            practiceInput.ifBlank { "Ejercicio completado en mi capítulo actual." }
                                        )
                                    },
                                    modifier = Modifier.testTag("complete_lesson_${lesson.code}")
                                ) {
                                    Text(if (isDone) "Actualizar Práctica" else "Completar Píldora Narrativa")
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Exportador Multiformato en un toque (Módulo 4.3)
                    item {
                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("multiformat_exporter_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Exportador Multiformato en Un Toque",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = "Genera tu manuscrito limpio y maquetado en formato PDF, EPUB o DOCX.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    ExportFormat.entries.forEach { format ->
                                        Button(
                                            onClick = { onExportFormat(format) },
                                            modifier = Modifier.testTag("export_button_${format.name}")
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Exportar ${format.name}")
                                        }
                                    }
                                }

                                if (lastExportResult != null) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text(
                                                text = lastExportResult.summaryMessage,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = lastExportResult.previewSnippet,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            OutlinedButton(
                                                onClick = {
                                                    ManuscriptExporter.shareExportedFile(context, lastExportResult)
                                                }
                                            ) {
                                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Compartir archivo ${lastExportResult.format.name}")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Lector Beta Social (Módulo 4.2)
                    item {
                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("social_beta_reader_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Lector Beta Social · Notas al Margen",
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                        Text(
                                            text = "Comparte tu borrador mediante enlace privado para recibir comentarios.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = activeProject?.isSharedBeta ?: false,
                                        onCheckedChange = onToggleBetaShare,
                                        modifier = Modifier.testTag("beta_share_switch")
                                    )
                                }

                                if (activeProject != null) {
                                    val shareLink = "https://storybuilder.app/beta/${activeProject.id.take(8)}"
                                    OutlinedButton(
                                        onClick = {
                                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(
                                                    Intent.EXTRA_TEXT,
                                                    "Te invito a ser Lector Beta de mi libro \"${activeProject.title}\" en StoryBuilder: $shareLink"
                                                )
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "Compartir enlace Beta"))
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Compartir Enlace Privado ($shareLink)")
                                    }
                                }

                                OutlinedTextField(
                                    value = reviewerName,
                                    onValueChange = { reviewerName = it },
                                    label = { Text("Nombre del amigo o Lector Beta") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = excerpt,
                                    onValueChange = { excerpt = it },
                                    label = { Text("Cita o párrafo comentado") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = commentText,
                                    onValueChange = { commentText = it },
                                    label = { Text("Comentario o nota al margen") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        onAddBetaComment(
                                            "Capítulo 1",
                                            reviewerName,
                                            excerpt,
                                            commentText,
                                            5L
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("add_beta_comment_button")
                                ) {
                                    Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Registrar Nota al Margen")
                                }
                            }
                        }
                    }

                    items(betaComments, key = { it.id }) { comment ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "${comment.reviewerName} · ${comment.chapterTitle} (★ ${comment.rating}/5)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                if (comment.paragraphExcerpt.isNotBlank()) {
                                    Text(
                                        text = comment.paragraphExcerpt,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(comment.commentText, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            else -> {
                TechnicalSpecificationTab()
            }
        }
    }
}

@Composable
private fun TechnicalSpecificationTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. Arquitectura Técnica y Tech Stack", style = MaterialTheme.typography.titleLarge)
                    Text("• Front-end / Móvil: Kotlin + Jetpack Compose (Material 3, Adaptive NavigationBar/Rail, Tipografía Editorial Playfair Display + Lora + Plus Jakarta Sans).", style = MaterialTheme.typography.bodyMedium)
                    Text("• Back-end & Persistencia: Firebase Authentication (Google Sign-In vía CredentialManager) + Cloud Firestore Enterprise con reglas Zero-Trust y sincronización offline-first.", style = MaterialTheme.typography.bodyMedium)
                    Text("• Motor de IA Narrativa: Gemini 3.5 Flash vía REST API (Prompt Studio, Show Don't Tell, Lector Beta Virtual) + Motor determinista local para detección instantánea de estilo.", style = MaterialTheme.typography.bodyMedium)
                    Text("• Exportación Nativa: Motor Android PdfDocument + empaquetador ZIP XHTML/OPF para EPUB y OpenXML para DOCX.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("2. Modelo de Datos (Entidades Principales)", style = MaterialTheme.typography.titleLarge)
                    Text("• User (users/{userId}): userId, displayName, dailyWordGoal, currentStreak, todayWords, completedLessonsCount.", style = MaterialTheme.typography.bodyMedium)
                    Text("• Project (projects/{projectId}): userId, title, genre, premise, structureTemplate (THREE_ACTS | HERO_JOURNEY | SNOWFLAKE), protagonistName, protagonistSecret, centralConflict, targetWordCount, currentWordCount, isSharedBeta.", style = MaterialTheme.typography.bodyMedium)
                    Text("• Character (characters/{characterId}): userId, projectId, name, role, motivation, fear, secret, appearance, evolutionArc.", style = MaterialTheme.typography.bodyMedium)
                    Text("• Chapter (chapters/{chapterId}): userId, projectId, chapterNumber, actLabel, title, synopsis, content, wordCount, status.", style = MaterialTheme.typography.bodyMedium)
                    Text("• TimelineEvent (timeline_events/{eventId}): userId, projectId, sequenceOrder, title, timeLabel, location, povCharacter, tensionLevel (1-10), notes.", style = MaterialTheme.typography.bodyMedium)
                    Text("• Lesson (lesson_progress/{progressId}) & BetaComment (beta_comments/{commentId}).", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("3. Mapa de Navegación y UX (User Journey)", style = MaterialTheme.typography.titleLarge)
                    Text("Paso 1 (Bienvenida): Inicio de sesión seguro con Google -> Panel con racha diaria.", style = MaterialTheme.typography.bodyMedium)
                    Text("Paso 2 (Story Wizard en 3 pasos): El usuario responde 4 preguntas sencillas y elige plantilla (3 Actos, Viaje del Héroe o Copo de Nieve).", style = MaterialTheme.typography.bodyMedium)
                    Text("Paso 3 (Andamiaje Automático): El sistema crea el proyecto, la ficha del protagonista, la escaleta de capítulos y el borrador inicial del Capítulo 1.", style = MaterialTheme.typography.bodyMedium)
                    Text("Paso 4 (Escritura Guiada): Redacción en el Editor Zen con Modo Enfoque, Pantalla Dividida y Co-Piloto IA.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("4. Roadmap de Desarrollo por Fases", style = MaterialTheme.typography.titleLarge)
                    Text("• Fase 1 (MVP): Story Wizard guiado, Plantillas en 3 Actos, Bible Cards de personajes, Editor Zen sin distracciones y exportación PDF.", style = MaterialTheme.typography.bodyMedium)
                    Text("• Fase 2 (Co-Piloto IA & Hábitos): Prompt Studio con Gemini, Detector de Coherencia, Asistente Show Don't Tell, Cronología Visual y rachas diarias.", style = MaterialTheme.typography.bodyMedium)
                    Text("• Fase 3 (Comunidad & Publicación): Lector Beta Virtual y Social con notas al margen, Píldoras Narrativas interactivas y exportación EPUB / DOCX.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
