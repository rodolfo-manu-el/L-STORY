package com.example.data.catalog

import com.example.data.model.MicroLesson
import com.example.data.model.NarrativeStructureOption

object NarrativeCatalog {

    val structureTemplates: List<NarrativeStructureOption> = listOf(
        NarrativeStructureOption(
            key = "THREE_ACTS",
            title = "Estructura en 3 Actos",
            subtitle = "Ideal para principiantes: Planteamiento, Confrontación y Desenlace claro.",
            defaultBeats = listOf(
                "Acto I · El Mundo Ordinario" to "Presenta al protagonista en su entorno cotidiano y muestra qué le falta.",
                "Acto I · El Incidente Incitador" to "Un suceso inesperado rompe la rutina y obliga al protagonista a actuar.",
                "Acto II · El Punto Medio" to "El secreto sale a la luz o el obstáculo principal muestra su verdadero poder.",
                "Acto II · La Noche Oscura del Alma" to "Todo parece perdido; el protagonista debe enfrentar su mayor miedo.",
                "Acto III · Clímax y Resolución" to "El enfrentamiento decisivo y el nuevo equilibrio tras la transformación."
            )
        ),
        NarrativeStructureOption(
            key = "HERO_JOURNEY",
            title = "El Viaje del Héroe",
            subtitle = "Estructura mítica de 5 fases para aventuras, fantasía y crecimiento interior.",
            defaultBeats = listOf(
                "Fase 1 · La Llamada a la Aventura" to "El protagonista recibe un desafío que cambiará su destino.",
                "Fase 2 · El Encuentro con el Mentor" to "Un guía entrega una herramienta, consejo o advertencia crucial.",
                "Fase 3 · Cruce del Umbral" to "El protagonista abandona lo conocido y entra en un mundo peligroso.",
                "Fase 4 · La Prueba Suprema (Ordalía)" to "Crisis central donde el protagonista enfrenta su sombra interior.",
                "Fase 5 · El Regreso con el Elixir" to "Vuelta a casa transformado con la clave para sanar su mundo."
            )
        ),
        NarrativeStructureOption(
            key = "SNOWFLAKE",
            title = "Método Copo de Nieve (Snowflake)",
            subtitle = "De lo simple a lo complejo: expande una frase en una novela paso a paso.",
            defaultBeats = listOf(
                "Paso 1 · Núcleo y Gancho Inicial" to "Establece la promesa narrativa y el conflicto inmediato en la primera escena.",
                "Paso 2 · Primer Giro de Complicación" to "El plan inicial del protagonista fracasa y las apuestas suben.",
                "Paso 3 · Revelación del Secreto" to "Una verdad oculta cambia las alianzas entre los personajes.",
                "Paso 4 · Cerco y Presión Máxima" to "Las opciones se agotan; el antagonista toma la ventaja.",
                "Paso 5 · Desenlace en Cascada" to "Resolución orgánica donde cada hilo argumental se cierra."
            )
        )
    )

    val microLessons: List<MicroLesson> = listOf(
        MicroLesson(
            code = "HOOKS_101",
            title = "Ganchos de Inicio que Atrapan en 3 Líneas",
            category = "Arranque",
            durationMinutes = 2,
            summary = "El primer párrafo no debe explicar la historia del mundo: debe abrir una pregunta urgente en la mente del lector.",
            goldenRule = "Empieza lo más tarde posible en la escena: justo cuando algo cambia o amenaza con romperse.",
            beforeExample = "Hacía sol en la ciudad de Valoria, fundada hace trescientos años tras la guerra de los reyes antiguos. Elena caminaba tranquila por la calle.",
            afterExample = "El reloj de la torre dio las trece campanadas, y Elena supo que tenía exactamente cuatro minutos antes de que su nombre apareciera en la lista negra.",
            interactivePrompt = "Reescribe la primera frase de tu capítulo añadiendo una contradicción, una cuenta atrás o un detalle fuera de lugar."
        ),
        MicroLesson(
            code = "SHOW_DONT_TELL",
            title = "Show, Don't Tell: Emociones que se Sienten",
            category = "Estilo",
            durationMinutes = 2,
            summary = "Nombrar una emoción ('estaba furioso', 'tenía miedo') informa al lector, pero mostrar la reacción física hace que el lector la viva.",
            goldenRule = "Prohíbete usar los verbos 'estar' y 'sentir' para describir emociones clave; usa gestos, respiración y objetos del entorno.",
            beforeExample = "Marcos estaba muy nervioso mientras esperaba la respuesta del juez en la sala.",
            afterExample = "Marcos hizo girar su anillo de cobre hasta dejarse una marca roja en el nudillo; el tic-tac del ventilador parecía golpearle la nuca.",
            interactivePrompt = "Elige un momento donde tu protagonista sienta miedo y descríbelo usando únicamente sus manos y un sonido del lugar."
        ),
        MicroLesson(
            code = "SUBTEXT_DIALOGUE",
            title = "Diálogos con Subtexto: Lo que Callan",
            category = "Diálogos",
            durationMinutes = 2,
            summary = "Las personas reales rara vez dicen exactamente lo que piensan. El mejor diálogo ocurre cuando los personajes hablan de algo trivial mientras pelean por algo profundo.",
            goldenRule = "Si dos personajes están enfadados, haz que discutan sobre quién dejó la ventana abierta en vez de explicar su rencor.",
            beforeExample = "—Estoy muy dolida porque me mentiste sobre el viaje, Julián —dijo Clara con tristeza.",
            afterExample = "—Tu maleta sigue teniendo la etiqueta del tren a Lisboa —dijo Clara sin levantar la vista de su taza fría—. Qué curioso, creí que en la costa no había estaciones.",
            interactivePrompt = "Escribe un intercambio de 4 líneas donde tu protagonista intente ocultar su gran secreto hablando del clima o de una comida."
        ),
        MicroLesson(
            code = "PACING_TENSION",
            title = "Control del Ritmo y la Tensión Dramática",
            category = "Trama",
            durationMinutes = 2,
            summary = "La longitud de tus frases controla el pulso cardíaco del lector. Las frases cortas aceleran la acción; las largas invitan a la reflexión.",
            goldenRule = "En escenas de peligro, recorta adjetivos y usa verbos de acción cortos. Después de una crisis, da espacio para respirar.",
            beforeExample = "Corrió rápidamente por el largo y oscuro pasillo de piedra mientras escuchaba los pesados pasos de su perseguidor acercándose.",
            afterExample = "Dobló la esquina. Piedra húmeda. Sin salida. Detrás, tres pasos metálicos resonaron en la oscuridad.",
            interactivePrompt = "Toma un párrafo de acción y divídelo en frases de menos de siete palabras para aumentar el vértigo."
        ),
        MicroLesson(
            code = "ANTAGONIST_MIRROR",
            title = "El Antagonista como Espejo del Héroe",
            category = "Personajes",
            durationMinutes = 2,
            summary = "Un gran villano o fuerza antagonista nunca se considera malvado a sí mismo: cree que es el héroe de su propia versión de la historia.",
            goldenRule = "Dale a tu antagonista un argumento moralmente comprensible pero un método inaceptable.",
            beforeExample = "El general Vane quería destruir el bosque porque odiaba la naturaleza y deseaba más oro.",
            afterExample = "El general Vane ordenó talar el bosque sagrado: era la única madera capaz de resistir el invierno que mataría de frío a los niños de la frontera.",
            interactivePrompt = "Define en una frase por qué el obstáculo o antagonista de tu historia cree que está haciendo lo correcto."
        ),
        MicroLesson(
            code = "BLANK_PAGE_CURE",
            title = "El Método del Primer Borrador Imperfecto",
            category = "Hábitos",
            durationMinutes = 2,
            summary = "El bloqueo creativo nace cuando intentamos escribir y editar al mismo tiempo. El único objetivo del primer borrador es existir.",
            goldenRule = "No puedes editar una página en blanco. Usa corchetes como [DESCRIBIR MERCADO AQUÍ] para no detener tu flujo y sigue adelante.",
            beforeExample = "(Pasar 40 minutos buscando el nombre exacto de una espada medieval antes de escribir la primera frase).",
            afterExample = "Empuñó la [ESPADA ANTIGUA] y cruzó el puente antes de que cayera la niebla.",
            interactivePrompt = "Escribe 3 frases seguidas sobre el próximo obstáculo de tu protagonista sin borrar ni una sola palabra."
        )
    )

    val dramaticTriggers: List<String> = listOf(
        "El protagonista recibe una carta escrita con su propia letra fechada cinco años en el futuro.",
        "Durante un apagón total, la única persona en quien confiaba desaparece dejando una llave oxidada.",
        "Un objeto cotidiano heredado empieza a señalar hacia un lugar que no figura en ningún mapa.",
        "Para salvar a su aliado, el protagonista debe revelar en público el secreto que juró proteger.",
        "El antagonista ofrece ayudar al protagonista a cambio de un único recuerdo de su infancia.",
        "Un desconocido pronuncia la frase exacta que el protagonista solo había pensado en silencio.",
        "El refugio seguro resulta haber sido construido por la misma organización de la que huyen."
    )

    val literarySynonyms: Map<String, Pair<List<String>, List<String>>> = mapOf(
        "miedo" to (listOf("pavor", "desasosiego", "estremecimiento", "vértigo", "congoja") to listOf("temple", "aplomo", "serenidad", "audacia")),
        "mirar" to (listOf("escrutar", "contemplar", "atisbar", "divisar", "escudriñar") to listOf("ignorar", "desviar", "rehuir")),
        "decir" to (listOf("murmurar", "mascullar", "proferir", "confesar", "sentenciar") to listOf("callar", "omitir", "silenciar")),
        "caminar" to (listOf("deambular", "avanzar", "merodear", "deslizarse", "irrumpir") to listOf("detenerse", "arraigarse", "reposar")),
        "oscuro" to (listOf("lúgubre", "tenebroso", "sombrío", "penumbroso", "opaco") to listOf("diáfano", "radiante", "resplandeciente")),
        "rápido" to (listOf("fugaz", "vertiginoso", "raudo", "súbito", "apremiante") to listOf("pausado", "parsimonioso", "moroso")),
        "triste" to (listOf("melancólico", "abatido", "desolado", "taciturno", "pesaroso") to listOf("jubiloso", "exultante", "radiante")),
        "secreto" to (listOf("arcano", "confidencia", "enigma", "misterio", "sigilo") to listOf("revelación", "evidencia", "clamor")),
        "luz" to (listOf("destello", "fulgor", "resplandor", "claridad", "vislumbre") to listOf("penumbra", "tiniebla", "sombra")),
        "silencio" to (listOf("mutismo", "quietud", "calma", "sigilo", "pausa") to listOf("estruendo", "fragor", "murmullo"))
    )
}
