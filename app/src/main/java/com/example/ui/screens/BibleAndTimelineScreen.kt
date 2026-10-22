package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.StoryCharacter
import com.example.data.model.TimelineEvent

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BibleAndTimelineScreen(
    characters: List<StoryCharacter>,
    timelineEvents: List<TimelineEvent>,
    onAddCharacter: (
        name: String,
        role: String,
        motivation: String,
        fear: String,
        secret: String,
        appearance: String,
        evolutionArc: String
    ) -> Unit,
    onDeleteCharacter: (String) -> Unit,
    onAddTimelineEvent: (
        sequenceOrder: Long,
        title: String,
        timeLabel: String,
        location: String,
        povCharacter: String,
        tensionLevel: Long,
        notes: String
    ) -> Unit,
    onDeleteTimelineEvent: (String) -> Unit
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }

    // Character form state
    var charName by remember { mutableStateOf("") }
    var charRole by remember { mutableStateOf("PROTAGONISTA") }
    var charMotivation by remember { mutableStateOf("") }
    var charFear by remember { mutableStateOf("") }
    var charSecret by remember { mutableStateOf("") }
    var charAppearance by remember { mutableStateOf("") }
    var charArc by remember { mutableStateOf("") }

    // Timeline form state
    var eventTitle by remember { mutableStateOf("") }
    var eventTimeLabel by remember { mutableStateOf("Día 2 · Atardecer") }
    var eventLocation by remember { mutableStateOf("Biblioteca sumergida") }
    var eventPov by remember { mutableStateOf(characters.firstOrNull()?.name ?: "Protagonista") }
    var eventTension by remember { mutableFloatStateOf(7f) }
    var eventNotes by remember { mutableStateOf("") }

    val roles = listOf("PROTAGONISTA", "ANTAGONISTA", "MENTOR", "ALIADO", "MUNDO_LORE")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedSubTab) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text("Bible Cards (${characters.size})") },
                icon = { Icon(Icons.Default.Groups, contentDescription = null) },
                modifier = Modifier.testTag("subtab_bible_cards")
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text("Cronología (${timelineEvents.size})") },
                icon = { Icon(Icons.Default.Timeline, contentDescription = null) },
                modifier = Modifier.testTag("subtab_timeline")
            )
        }

        if (selectedSubTab == 0) {
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
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Nueva Ficha de Personaje o Lore (Bible Card)",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Define motivaciones, miedos y arco de transformación para mantener coherencia psicológica.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                value = charName,
                                onValueChange = { charName = it },
                                label = { Text("Nombre del personaje o lugar de Lore") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("bible_name_input")
                            )

                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                roles.forEach { role ->
                                    FilterChip(
                                        selected = charRole == role,
                                        onClick = { charRole = role },
                                        label = { Text(role) }
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = charMotivation,
                                    onValueChange = { charMotivation = it },
                                    label = { Text("Motivación") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = charFear,
                                    onValueChange = { charFear = it },
                                    label = { Text("Mayor miedo") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            OutlinedTextField(
                                value = charSecret,
                                onValueChange = { charSecret = it },
                                label = { Text("Secreto oculto o regla del mundo") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = charAppearance,
                                    onValueChange = { charAppearance = it },
                                    label = { Text("Apariencia / Rasgos") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = charArc,
                                    onValueChange = { charArc = it },
                                    label = { Text("Arco de evolución") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Button(
                                onClick = {
                                    onAddCharacter(
                                        charName.ifBlank { "Capitán Thorne" },
                                        charRole,
                                        charMotivation.ifBlank { "Proteger la última brújula astral" },
                                        charFear.ifBlank { "Repetir la traición de su pasado" },
                                        charSecret.ifBlank { "Conoce el paradero del heredero" },
                                        charAppearance.ifBlank { "Gabardina gastada y reloj sin agujas" },
                                        charArc.ifBlank { "De mercenario escéptico a mentor leal" }
                                    )
                                    charName = ""
                                    charMotivation = ""
                                    charFear = ""
                                    charSecret = ""
                                    charAppearance = ""
                                    charArc = ""
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("add_character_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Guardar Ficha en la Biblia")
                            }
                        }
                    }
                }

                items(characters, key = { it.id }) { char ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = char.name,
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                        Text(
                                            text = char.role,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                                IconButton(onClick = { onDeleteCharacter(char.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar ficha")
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("• Motivación: ${char.motivation}", style = MaterialTheme.typography.bodyMedium)
                            Text("• Miedo: ${char.fear}", style = MaterialTheme.typography.bodyMedium)
                            Text("• Secreto: ${char.secret}", style = MaterialTheme.typography.bodyMedium)
                            Text("• Apariencia: ${char.appearance}", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = "Arco de Evolución: ${char.evolutionArc}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        } else {
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
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Cronología Visual y Curva de Tensión",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Ordena escenas cronológicamente para evitar agujeros de guion e inconsistencias temporales.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                value = eventTitle,
                                onValueChange = { eventTitle = it },
                                label = { Text("Suceso o escena clave") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("timeline_title_input")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = eventTimeLabel,
                                    onValueChange = { eventTimeLabel = it },
                                    label = { Text("Momento temporal") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = eventLocation,
                                    onValueChange = { eventLocation = it },
                                    label = { Text("Ubicación") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            OutlinedTextField(
                                value = eventPov,
                                onValueChange = { eventPov = it },
                                label = { Text("Personaje Punto de Vista (POV)") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = "Nivel de Tensión Dramática: ${eventTension.toInt()} / 10",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Slider(
                                value = eventTension,
                                onValueChange = { eventTension = it },
                                valueRange = 1f..10f,
                                steps = 8
                            )

                            OutlinedTextField(
                                value = eventNotes,
                                onValueChange = { eventNotes = it },
                                label = { Text("Consecuencia o pista sembrada (Foreshadowing)") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    val nextOrder = (timelineEvents.maxOfOrNull { it.sequenceOrder } ?: 0L) + 1L
                                    onAddTimelineEvent(
                                        nextOrder,
                                        eventTitle.ifBlank { "Descubrimiento del pasadizo" },
                                        eventTimeLabel,
                                        eventLocation,
                                        eventPov,
                                        eventTension.toLong(),
                                        eventNotes.ifBlank { "El protagonista encuentra la segunda mitad del mapa." }
                                    )
                                    eventTitle = ""
                                    eventNotes = ""
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("add_timeline_event_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Añadir Hito a la Línea de Tiempo")
                            }
                        }
                    }
                }

                items(timelineEvents, key = { it.id }) { ev ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = "#${ev.sequenceOrder} · ${ev.timeLabel}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }
                                IconButton(onClick = { onDeleteTimelineEvent(ev.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar evento")
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(ev.title, style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = "Lugar: ${ev.location} · POV: ${ev.povCharacter}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            if (ev.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(ev.notes, style = MaterialTheme.typography.bodyMedium)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tensión dramática: ${ev.tensionLevel}/10",
                                style = MaterialTheme.typography.labelMedium
                            )
                            LinearProgressIndicator(
                                progress = { (ev.tensionLevel.toFloat() / 10f).coerceIn(0.1f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(50))
                            )
                        }
                    }
                }
            }
        }
    }
}
