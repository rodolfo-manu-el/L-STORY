package com.example.data.ai

import com.example.BuildConfig
import com.example.data.catalog.NarrativeCatalog
import com.example.data.model.BetaReaderReport
import com.example.data.model.StyleIssue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String? = null
)

@Serializable
data class GenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate> = emptyList()
)

@Serializable
data class Candidate(
    val content: Content? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object NarrativeAiService {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val json = Json { ignoreUnknownKeys = true }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val api: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GeminiApiService::class.java)
    }

    private fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    suspend fun generateStoryIdeas(genre: String, seedTopic: String): List<String> = withContext(Dispatchers.IO) {
        if (isApiKeyConfigured()) {
            try {
                val prompt = """
                    Actúa como un consultor narrativo senior para escritores novatos.
                    Genera exactamente 3 ideas de historias originales y evocadoras en español para el género "$genre" inspiradas en: "${seedTopic.ifBlank { "un secreto que cambia el destino" }}".
                    Separa cada idea con una línea en blanco y que cada una incluya: Premisa, Secreto del Protagonista y Gancho de Inicio.
                """.trimIndent()
                val response = api.generateContent(
                    apiKey = BuildConfig.GEMINI_API_KEY,
                    request = GenerateContentRequest(
                        contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                        generationConfig = GenerationConfig(temperature = 0.8f)
                    )
                )
                val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text.orEmpty()
                val ideas = text.split("\n\n").map { it.trim() }.filter { it.length > 25 }
                if (ideas.isNotEmpty()) return@withContext ideas.take(4)
            } catch (_: Exception) {
                // Fallback to curated creative generator below
            }
        }
        val cleanTopic = seedTopic.trim().ifBlank { "un archivo prohibido" }
        listOf(
            "[$genre] Premisa: Al investigar $cleanTopic, tu protagonista descubre que su propia familia firmó un pacto hace treinta años. Secreto: Solo él puede escuchar el eco del suceso. Gancho: Empieza en el instante en que recibe una llave sin remitente.",
            "[$genre] Premisa: En una ciudad donde los recuerdos se guardan en frascos de ámbar, $cleanTopic provoca la desaparición del mentor principal. Secreto: El protagonista ocultó la última página del diario. Gancho: Alguien llama a la puerta a medianoche usando una contraseña olvidada.",
            "[$genre] Premisa: Dos desconocidos quedan atrapados durante una tormenta mientras buscan $cleanTopic. Secreto: Uno de ellos fue enviado para impedir el hallazgo, pero cambia de bando. Gancho: El mapa que llevan muestra una habitación que no debería existir."
        )
    }

    suspend fun transformShowDontTell(flatSentence: String): List<String> = withContext(Dispatchers.IO) {
        val cleanInput = flatSentence.trim().ifBlank { "Tenía mucho miedo al entrar en la habitación oscura." }
        if (isApiKeyConfigured()) {
            try {
                val prompt = """
                    Eres un editor literario experto en la técnica "Show, Don't Tell" (Mostrar, no contar).
                    Transforma esta frase descriptiva plana en 3 alternativas sensoriales distintas en español:
                    1. Enfoque Sensorial (sonido, temperatura, tacto)
                    2. Enfoque en Lenguaje Corporal y Acción
                    3. Enfoque en Atmósfera y Tensión Dramática
                    Frase original: "$cleanInput"
                    Separa cada alternativa con un salto de línea doble.
                """.trimIndent()
                val response = api.generateContent(
                    apiKey = BuildConfig.GEMINI_API_KEY,
                    request = GenerateContentRequest(
                        contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                        generationConfig = GenerationConfig(temperature = 0.75f)
                    )
                )
                val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text.orEmpty()
                val items = text.split("\n\n").map { it.trim() }.filter { it.length > 15 }
                if (items.isNotEmpty()) return@withContext items.take(3)
            } catch (_: Exception) {
                // Curated sensory fallback below
            }
        }
        listOf(
            "Sensorial (Oído y Tacto): El pomo helado le entumeció los dedos; al empujar la madera hinchada, un olor a ceniza húmeda y el crujido de un cristal bajo su bota le secaron la garganta.",
            "Lenguaje Corporal y Acción: Contuvo el aliento en el umbral, pegó los hombros al marco de la puerta y buscó a tientas el interruptor mientras el pulso le golpeaba en las sienes.",
            "Atmósfera y Subtexto: La oscuridad del cuarto no parecía vacía: en el rincón junto al espejo, la cortina seguía meciéndose aunque todas las ventanas estaban cerradas."
        )
    }

    fun analyzeCoherenceAndStyleLocal(text: String): List<StyleIssue> {
        val issues = mutableListOf<StyleIssue>()
        val clean = text.trim()
        if (clean.isEmpty()) return emptyList()

        // 1. Overly long paragraphs
        val paragraphs = clean.split(Regex("\n+")).map { it.trim() }.filter { it.isNotEmpty() }
        paragraphs.forEachIndexed { idx, para ->
            val words = para.split(Regex("\\s+"))
            if (words.size > 95) {
                issues.add(
                    StyleIssue(
                        category = "Párrafo Extenso (#${idx + 1})",
                        severity = "ALTA",
                        excerpt = para.take(90) + "...",
                        explanation = "Este párrafo tiene ${words.size} palabras. Los bloques mayores a 95 palabras ralentizan la lectura móvil y diluyen la tensión.",
                        replacementSuggestion = "Divide el párrafo en dos: corta justo cuando cambie la acción del personaje o tras un diálogo."
                    )
                )
            }
        }

        // 2. Excess -mente adverbs
        val menteRegex = Regex("\\b([a-zA-ZáéíóúÁÉÍÓÚñÑ]+mente)\\b", RegexOption.IGNORE_CASE)
        val menteMatches = menteRegex.findAll(clean).map { it.value }.toList()
        if (menteMatches.size >= 2) {
            issues.add(
                StyleIssue(
                    category = "Exceso de Adverbios en -mente",
                    severity = "MEDIA",
                    excerpt = menteMatches.distinct().joinToString(", "),
                    explanation = "Se detectaron ${menteMatches.size} adverbios terminados en '-mente'. Suelen debilitar la fuerza del verbo principal.",
                    replacementSuggestion = "Sustituye '${menteMatches.first()}' por un verbo de acción concreto o un gesto físico."
                )
            )
        }

        // 3. Passive voice detection
        val passiveRegex = Regex("\\b(fue|fueron|era|eran|sido)\\s+([a-zA-Záéíóúñ]+(?:ado|ada|ido|ida))\\b", RegexOption.IGNORE_CASE)
        val passiveMatch = passiveRegex.find(clean)
        if (passiveMatch != null) {
            issues.add(
                StyleIssue(
                    category = "Voz Pasiva Detectada",
                    severity = "MEDIA",
                    excerpt = passiveMatch.value,
                    explanation = "La voz pasiva resta inmediatez a la escena y oculta quién ejecuta la acción dramática.",
                    replacementSuggestion = "Convierte la frase a voz activa colocando al sujeto ejecutor al principio de la oración."
                )
            )
        }

        // 4. Flat telling words ("estaba muy", "sintió mucho", "había un")
        val tellingRegex = Regex("\\b(estaba muy|tenía mucho|sintió mucho|era muy|había una? gran)\\b", RegexOption.IGNORE_CASE)
        val tellingMatch = tellingRegex.find(clean)
        if (tellingMatch != null) {
            issues.add(
                StyleIssue(
                    category = "Descripción Plana (Telling)",
                    severity = "SUGERENCIA",
                    excerpt = tellingMatch.value,
                    explanation = "Expresiones como '${tellingMatch.value}' resumen la emoción en lugar de permitir que el lector la experimente.",
                    replacementSuggestion = "Usa el módulo 'Show, Don't Tell' para traducir esta frase en una sensación física o acción."
                )
            )
        }

        // 5. Word repetitions
        val stopWords = setOf(
            "para", "como", "pero", "sobre", "entre", "hasta", "desde", "cuando",
            "donde", "porque", "aquel", "aquella", "había", "estaba", "tenía", "todos"
        )
        val wordCounts = clean.lowercase()
            .split(Regex("[^a-záéíóúñ]+"))
            .filter { it.length >= 5 && it !in stopWords }
            .groupingBy { it }
            .eachCount()
            .filter { it.value >= 4 }

        wordCounts.entries.take(2).forEach { (word, count) ->
            issues.add(
                StyleIssue(
                    category = "Repetición Léxica",
                    severity = "SUGERENCIA",
                    excerpt = "\"$word\" ($count veces)",
                    explanation = "La palabra '$word' aparece $count veces en el capítulo actual.",
                    replacementSuggestion = "Consulta el Asistente de Lenguaje en el Editor Zen para variar con sinónimos contextuales."
                )
            )
        }

        if (issues.isEmpty()) {
            issues.add(
                StyleIssue(
                    category = "Prosa Equilibrada",
                    severity = "SUGERENCIA",
                    excerpt = "Ritmo fluido y longitud de párrafos óptima",
                    explanation = "No se detectaron excesos de voz pasiva, adverbios repetitivos ni bloques densos.",
                    replacementSuggestion = "Prueba el Lector Beta Virtual para evaluar la tensión dramática y el subtexto de los diálogos."
                )
            )
        }

        return issues
    }

    suspend fun runVirtualBetaReader(chapterTitle: String, content: String): BetaReaderReport = withContext(Dispatchers.IO) {
        val clean = content.trim()
        val words = if (clean.isEmpty()) 0 else clean.split(Regex("\\s+")).size
        val hasDialogue = clean.contains("—") || clean.contains("\"") || clean.contains("-")
        val shortSentences = clean.split(Regex("[.!?]+")).count { it.trim().split(Regex("\\s+")).size in 2..9 }

        val baseScore = (72 + (if (hasDialogue) 10 else 0) + (shortSentences.coerceAtMost(10))).coerceIn(65, 96)
        val tension = (5 + (shortSentences / 3) + (if (words > 120) 2 else 1)).coerceIn(4, 10)

        var aiCritique: String? = null
        if (isApiKeyConfigured() && clean.length > 30) {
            try {
                val prompt = """
                    Actúa como un Lector Beta literario constructivo y empático para un escritor novato.
                    Analiza en 3 viñetas breves el capítulo "$chapterTitle":
                    Texto: "${clean.take(2500)}"
                    Evalúa: 1) Ritmo e interés inicial, 2) Tensión dramática, 3) Una mejora concreta de diálogo o atmósfera.
                """.trimIndent()
                val response = api.generateContent(
                    apiKey = BuildConfig.GEMINI_API_KEY,
                    request = GenerateContentRequest(
                        contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                        generationConfig = GenerationConfig(temperature = 0.7f)
                    )
                )
                aiCritique = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
            } catch (_: Exception) {
                aiCritique = null
            }
        }

        BetaReaderReport(
            overallScore = baseScore,
            pacingVerdict = when {
                words < 80 -> "Ritmo en construcción ($words palabras). Añade un giro sensorial o conflicto al final de la escena para enganchar al siguiente capítulo."
                shortSentences >= 5 -> "Ritmo ágil y dinámico. La alternancia entre oraciones breves e imágenes mantiene la atención del lector."
                else -> "Ritmo pausado y atmosférico. Ideal para inmersión, aunque conviene intercalar una acción directa a mitad de escena."
            },
            tensionScore = tension,
            dialogueVerdict = if (hasDialogue) {
                "Se detectan intervenciones de diálogo que oxigenan la página. Sugerencia: acompaña al menos un parlamento con un gesto contradictorio (subtexto)."
            } else {
                "Capítulo predominantemente narrativo. Incluir un breve intercambio de diálogo de 2 o 3 líneas elevará la inmediatez dramática."
            },
            sensoryAlternatives = listOf(
                "Vista / Luz" to "Añade cómo incide la iluminación sobre el rostro o las manos del protagonista.",
                "Sonido ambiente" to "Introduce un sonido de fondo recurrente que marque el paso del tiempo en la escena."
            ),
            strengths = listOf(
                "Claridad en la premisa emocional del capítulo \"$chapterTitle\".",
                "Voz narrativa accesible y bien enfocada en el punto de vista del protagonista.",
                if (!aiCritique.isNullOrBlank()) "Análisis IA: ${aiCritique.take(240)}" else "Buena progresión hacia el conflicto central del proyecto."
            ),
            improvements = listOf(
                "Cierra el capítulo con una pregunta abierta o una decisión difícil (Cliffhanger suave).",
                "Revisa el primer párrafo para activar al menos dos sentidos (oído, tacto o temperatura).",
                "Asegúrate de que el secreto del protagonista influya en lo que decide callar en esta escena."
            )
        )
    }

    suspend fun lookupSynonymsAndGrammar(wordOrPhrase: String): Pair<List<String>, List<String>> = withContext(Dispatchers.IO) {
        val key = wordOrPhrase.trim().lowercase()
        NarrativeCatalog.literarySynonyms[key]?.let { return@withContext it }

        if (isApiKeyConfigured() && key.isNotEmpty()) {
            try {
                val prompt = """
                    Para la palabra o expresión en español "$key", responde en dos líneas exactas:
                    SINONIMOS: 5 sinónimos literarios separados por coma
                    ANTONIMOS: 3 antónimos separados por coma
                """.trimIndent()
                val response = api.generateContent(
                    apiKey = BuildConfig.GEMINI_API_KEY,
                    request = GenerateContentRequest(
                        contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                        generationConfig = GenerationConfig(temperature = 0.4f)
                    )
                )
                val raw = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text.orEmpty()
                val synLine = raw.lines().firstOrNull { it.uppercase().startsWith("SINONIMOS:") }
                    ?.substringAfter(":")?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
                val antLine = raw.lines().firstOrNull { it.uppercase().startsWith("ANTONIMOS:") }
                    ?.substringAfter(":")?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
                if (!synLine.isNullOrEmpty()) {
                    return@withContext (synLine to (antLine ?: listOf("Contraste directo")))
                }
            } catch (_: Exception) {
                // Fallback below
            }
        }

        listOf(
            "evocar", "vislumbrar", "matizar", "perfilar", "intensificar"
        ) to listOf(
            "atenuar", "silenciar", "disipar"
        )
    }
}
