package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.BetaReaderReport
import com.example.data.model.StoryChapter
import com.example.data.model.StyleIssue

@Composable
fun AiCopilotScreen(
    activeChapter: StoryChapter?,
    isAiLoading: Boolean,
    styleIssues: List<StyleIssue>,
    showDontTellOptions: List<String>,
    betaReport: BetaReaderReport?,
    onRunCoherenceAudit: (String) -> Unit,
    onRunShowDontTell: (String) -> Unit,
    onRunVirtualBeta: (String, String) -> Unit,
    onAppendToActiveChapter: (String) -> Unit
) {
    var flatSentence by remember {
        mutableStateOf("Estaba muy asustada cuando entró al salón oscuro y sintió mucho frío.")
    }

    val sampleText = activeChapter?.content?.ifBlank {
        "Caminó rápidamente por el pasillo oscuro mientras pensaba solemnemente en el secreto que había sido ocultado por su mentor. Tenía mucho miedo de lo que encontraría."
    } ?: "Caminó rápidamente por el pasillo oscuro mientras pensaba solemnemente en el secreto que había sido ocultado por su mentor."

    LaunchedEffect(activeChapter?.id) {
        onRunCoherenceAudit(sampleText)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ai_copilot_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Asistente "Show, Don't Tell" (Módulo 3.2)
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("show_dont_tell_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Asistente \"Show, Don't Tell\"",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Transforma descripciones planas en imágenes sensoriales y dinámicas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedTextField(
                        value = flatSentence,
                        onValueChange = { flatSentence = it },
                        label = { Text("Escribe una frase plana ('telling') para enriquecer") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("show_dont_tell_input")
                    )

                    Button(
                        onClick = { onRunShowDontTell(flatSentence) },
                        enabled = !isAiLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("run_show_dont_tell_button")
                    ) {
                        if (isAiLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Generar Alternativas Sensoriales")
                    }

                    showDontTellOptions.forEach { option ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(text = option, style = MaterialTheme.typography.bodyMedium)
                                if (activeChapter != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedButton(onClick = { onAppendToActiveChapter(option) }) {
                                        Text("Insertar en ${activeChapter.title.take(20)}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Detector de Coherencia y Estilo (Módulo 3.1)
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("coherence_detector_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.Radar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Detector de Coherencia y Estilo",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = "Analiza párrafos largos, repeticiones, adverbios -mente y voz pasiva",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = { onRunCoherenceAudit(sampleText) },
                            modifier = Modifier.testTag("run_coherence_button")
                        ) {
                            Text("Analizar")
                        }
                    }
                }
            }
        }

        items(styleIssues) { issue ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = issue.category,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = issue.severity,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Detectado: ${issue.excerpt}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = issue.explanation,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sugerencia: ${issue.replacementSuggestion}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // 3. Lector Beta Virtual (Módulo 3.3)
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("virtual_beta_reader_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Lector Beta Virtual (Simulación IA)",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Evalúa ritmo, tensión dramática y solidez de los diálogos de tu capítulo",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = {
                            onRunVirtualBeta(
                                activeChapter?.title ?: "Capítulo 1",
                                sampleText
                            )
                        },
                        enabled = !isAiLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("run_virtual_beta_button")
                    ) {
                        Text("Ejecutar Simulación de Lector Beta")
                    }

                    if (betaReport != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Índice de Inmersión Literaria",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "${betaReport.overallScore}/100",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { betaReport.overallScore / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(50))
                                )
                                Text(
                                    text = "Ritmo: ${betaReport.pacingVerdict}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Diálogos: ${betaReport.dialogueVerdict}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Puntos Fuertes:",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                                betaReport.strengths.forEach { st ->
                                    Text("✓ $st", style = MaterialTheme.typography.bodySmall)
                                }
                                Text(
                                    text = "Oportunidades de Mejora:",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                betaReport.improvements.forEach { imp ->
                                    Text("• $imp", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
