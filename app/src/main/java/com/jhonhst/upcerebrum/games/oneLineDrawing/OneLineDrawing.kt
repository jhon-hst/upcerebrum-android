package com.jhonhst.upcerebrum.games.oneLineDrawing

// Los imports SIEMPRE van aquí arriba, justo después del package
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

// ─── ESTADO DEL JUEGO ────────────────────────────────────────────────────────
// Enum formal para los estados del juego, en vez de usar Strings sueltos como "won" o "lost".
// Así el compilador nos avisa si escribimos mal un estado.
enum class GameState {
    IDLE,    // El jugador no ha empezado a trazar aún
    PLAYING, // El jugador está arrastrando el dedo actualmente
    WON,     // Dibujó todas las líneas sin repetir → ganó
    LOST     // Levantó el dedo a mitad del camino, o se quedó bloqueado → perdió
}

// ─── FUNCIÓN AUXILIAR ────────────────────────────────────────────────────────
// Comprueba si una arista entre el nodo 'a' y el nodo 'b' ya fue dibujada.
// Funciona en ambas direcciones: (1→2) es lo mismo que (2→1).
fun hasEdge(edges: List<GameEdge>, a: Int, b: Int): Boolean {
    return edges.any { (it.nodeA == a && it.nodeB == b) || (it.nodeA == b && it.nodeB == a) }
}

@Composable
fun OneLineDrawing(levelData: LevelData) {

    // ── ESTADOS PRINCIPALES ──────────────────────────────────────────────────

    // Lista de aristas que el jugador ya trazó con éxito
    var drawnEdges by remember { mutableStateOf<List<GameEdge>>(emptyList()) }

    // ID del nodo donde está parado el "lápiz" en este momento
    var currentNodeId by remember { mutableStateOf<Int?>(null) }

    // ── ESTADOS DE ANIMACIÓN TÁCTIL ──────────────────────────────────────────

    // ID del nodo al que el jugador está apuntando (pero aún no llega)
    var activeTargetId by remember { mutableStateOf<Int?>(null) }

    // Qué tan avanzado va el trazo hacia el nodo destino (0.0 = inicio, 1.0 = llegó)
    var activeProgress by remember { mutableFloatStateOf(0f) }

    // ── ESTADO GENERAL Y FEEDBACK VISUAL ─────────────────────────────────────

    // Estado actual de la partida (ver enum arriba)
    var gameState by remember { mutableStateOf(GameState.IDLE) }

    // Posición en pantalla donde el jugador falló (para dibujar la X roja)
    var failurePoint by remember { mutableStateOf<Offset?>(null) }

    // ── TOLERANCIAS TÁCTILES ─────────────────────────────────────────────────
    // nodeStartTolerance: radio (en px) para detectar que el dedo tocó un nodo al empezar
    val nodeStartTolerance = 100f
    // lineDragTolerance: distancia máxima que puede alejarse el dedo de una línea mientras arrastra
    val lineDragTolerance = 500f

    // ── AUTO-REINICIO ─────────────────────────────────────────────────────────
    // Este bloque se ejecuta cada vez que 'gameState' cambia.
    // Si el nuevo estado es LOST, esperamos 1 segundo y luego reseteamos todo.
    LaunchedEffect(gameState) {
        if (gameState == GameState.LOST) {
            delay(1000L.milliseconds) // Espera 1 segundo para que el jugador vea la X roja
            // Reseteo completo del juego
            gameState = GameState.IDLE
            drawnEdges = emptyList()
            activeTargetId = null
            activeProgress = 0f
            failurePoint = null
            currentNodeId = null
        }
    }

    // ── CANVAS PRINCIPAL ──────────────────────────────────────────────────────
    // Canvas es el componente de Compose donde dibujamos todo con primitivas (líneas, círculos, etc.)
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF243447)) // Fondo azul oscuro del tablero
            .pointerInput(levelData) {     // pointerInput detecta gestos táctiles; se reinicia si cambia levelData
                detectDragGestures(

                    // ── INICIO DEL ARRASTRE ───────────────────────────────────
                    // Se llama una sola vez cuando el jugador pone el dedo en la pantalla
                    onDragStart = { startOffset ->

                        // Si ya ganó o está en medio del auto-reinicio, ignoramos el toque
                        if (gameState == GameState.WON || gameState == GameState.LOST) return@detectDragGestures

                        // size.width y size.height son Float dentro de pointerInput → tipos correctos
                        val width: Float = size.width.toFloat()
                        val height: Float = size.height.toFloat()

                        // Buscamos si el dedo tocó cerca de algún nodo del nivel
                        val startNode = levelData.nodes.find { node ->
                            (node.toOffset(width, height) - startOffset).getDistance() < nodeStartTolerance
                        }

                        // ✅ FIX: Solo reseteamos y empezamos si el toque fue sobre un nodo válido.
                        // Antes el reset ocurría siempre, borrando el progreso aunque el dedo
                        // cayera en el vacío. Ahora el reset queda aquí adentro.
                        if (startNode != null) {
                            drawnEdges = emptyList()
                            activeTargetId = null
                            activeProgress = 0f
                            failurePoint = null
                            currentNodeId = startNode.id
                            gameState = GameState.PLAYING
                        }
                    },

                    // ── DURANTE EL ARRASTRE ───────────────────────────────────
                    // Se llama continuamente mientras el dedo se mueve por la pantalla
                    onDrag = { change, _ ->

                        // Si no estamos jugando o no hay nodo activo, ignoramos el movimiento
                        if (gameState != GameState.PLAYING || currentNodeId == null) return@detectDragGestures

                        // size.width y size.height son Float dentro de pointerInput → tipos correctos
                        val width: Float = size.width.toFloat()
                        val height: Float = size.height.toFloat()
                        val currentPos: Offset = change.position // Posición actual del dedo en pantalla

                        // Posición en pantalla del nodo donde está parado el lápiz
                        val currentOffset: Offset = levelData.nodes
                            .first { it.id == currentNodeId }
                            .toOffset(width, height)

                        // ── NUEVO ENFOQUE: IDENTIFICAR EL NODO DE RETROCESO ──
                        // Revisamos cuál fue la última línea dibujada para saber de dónde venimos
                        val retreatNodeId = drawnEdges.lastOrNull()?.let { lastEdge ->
                            if (lastEdge.nodeA == currentNodeId) lastEdge.nodeB else lastEdge.nodeA
                        }

                        // Filtramos TODAS las aristas que conectan con el nodo actual
                        val validEdges = levelData.edges.filter {
                            it.nodeA == currentNodeId || it.nodeB == currentNodeId
                        }

                        // VECINOS EVALUABLES: Los nodos a los que aún no hemos ido + el nodo del que venimos
                        val evaluableNeighbors = validEdges
                            .map { if (it.nodeA == currentNodeId) it.nodeB else it.nodeA }
                            .filter { neighborId ->
                                !hasEdge(drawnEdges, currentNodeId!!, neighborId) || neighborId == retreatNodeId
                            }

                        // Variables para encontrar el mejor candidato hacia donde apunta el dedo
                        var bestNeighbor: Int? = null
                        var bestProg = 0f
                        var minDist = Float.MAX_VALUE

                        // Recorremos cada vecino evaluable y calculamos si el dedo apunta hacia él
                        for (neighborId in evaluableNeighbors) {
                            val targetOffset: Offset = levelData.nodes
                                .first { it.id == neighborId }
                                .toOffset(width, height)

                            // Vector de la línea y Vector del dedo
                            val lineVec: Offset = targetOffset - currentOffset
                            val touchVec: Offset = currentPos - currentOffset

                            val lineLenSq: Float = lineVec.x * lineVec.x + lineVec.y * lineVec.y
                            if (lineLenSq > 0f) {
                                // 't' es la proyección del dedo sobre la línea
                                val t: Float = (touchVec.x * lineVec.x + touchVec.y * lineVec.y) / lineLenSq
                                val clampedT: Float = t.coerceIn(0f, 1f)

                                // Punto más cercano sobre la línea al dedo actual
                                val projection: Offset = currentOffset + (lineVec * clampedT)
                                val distToLine: Float = (currentPos - projection).getDistance()

                                // Si el dedo está en tolerancia Y es la línea más cercana hasta ahora
                                if (distToLine < lineDragTolerance && distToLine < minDist) {
                                    minDist = distToLine
                                    bestNeighbor = neighborId
                                    bestProg = clampedT
                                }
                            }
                        }

                        if (bestNeighbor != null) {

                            // ── LÓGICA DE DESHACER (RETROCEDER) EXACTA ──
                            // Si la línea ganadora es por la que venimos Y avanzamos 50% hacia atrás
                            if (bestNeighbor == retreatNodeId && bestProg > 0.50f) {
                                drawnEdges = drawnEdges.dropLast(1) // Borramos la última línea completada
                                val oldCurrentId = currentNodeId!!
                                currentNodeId = retreatNodeId       // Nos paramos en el nodo anterior

                                // Invertimos la animación para que el retroceso sea 100% fluido
                                activeTargetId = oldCurrentId
                                activeProgress = 1f - bestProg
                                return@detectDragGestures // Terminamos por este frame
                            }

                            // ── LÓGICA DE AVANCE NORMAL ──
                            activeTargetId = bestNeighbor
                            activeProgress = bestProg

                            // Si el progreso supera el 90%, consideramos que llegó al nodo destino
                            if (bestProg > 0.90f) {
                                drawnEdges = drawnEdges + GameEdge(currentNodeId!!, bestNeighbor)
                                currentNodeId = bestNeighbor
                                activeTargetId = null
                                activeProgress = 0f

                                // ¿Dibujó todas las aristas del nivel? → GANÓ
                                if (drawnEdges.size == levelData.edges.size) {
                                    gameState = GameState.WON
                                } else {
                                    // Revisamos si desde el nuevo nodo quedan aristas disponibles
                                    val nextValidEdges = levelData.edges.filter {
                                        it.nodeA == currentNodeId || it.nodeB == currentNodeId
                                    }
                                    val nextUnvisited = nextValidEdges.filter { edge ->
                                        !hasEdge(drawnEdges, edge.nodeA, edge.nodeB)
                                    }

                                    // Si no hay vecinos sin visitar → BLOQUEADO → perdió
                                    if (nextUnvisited.isEmpty()) {
                                        gameState = GameState.LOST
                                        failurePoint = levelData.nodes
                                            .first { it.id == currentNodeId }
                                            .toOffset(width, height)
                                    }
                                }
                            }
                        } else {
                            // El dedo no apunta a ninguna línea válida → limpiamos el preview
                            activeTargetId = null
                            activeProgress = 0f
                        }
                    },

                    // ── FIN DEL ARRASTRE ──────────────────────────────────────
                    // Se llama cuando el jugador levanta el dedo
                    onDragEnd = {
                        // Si levantó el dedo sin terminar el dibujo → PERDIÓ
                        if (gameState == GameState.PLAYING) {
                            gameState = GameState.LOST

                            // size.width y size.height son Float dentro de pointerInput → tipos correctos
                            val width: Float = size.width.toFloat()
                            val height: Float = size.height.toFloat()

                            // Calculamos dónde mostrar la X roja de fallo
                            if (activeTargetId != null && currentNodeId != null) {
                                // Si estaba a mitad de una línea, la X va en ese punto intermedio
                                val currentOffset = levelData.nodes
                                    .first { it.id == currentNodeId }
                                    .toOffset(width, height)
                                val targetOffset = levelData.nodes
                                    .first { it.id == activeTargetId }
                                    .toOffset(width, height)
                                val lineVec: Offset = targetOffset - currentOffset
                                failurePoint = currentOffset + (lineVec * activeProgress)
                            } else if (currentNodeId != null) {
                                // Si no estaba arrastrando hacia ningún vecino, la X va en el nodo actual
                                failurePoint = levelData.nodes
                                    .first { it.id == currentNodeId }
                                    .toOffset(width, height)
                            }
                        }
                    }
                )
            }
    ) {
        // ── BLOQUE DE DIBUJO ─────────────────────────────────────────────────
        // Todo lo que está aquí dentro se redibufa cada vez que cambia un estado.
        // Dentro del bloque Canvas { } size.width y size.height son Float directamente.
        val width: Float = size.width
        val height: Float = size.height
        val strokeW = 32f // Grosor de las líneas en píxeles

        // Paleta de colores
        val baseLineColor = Color(0xFF4A6B8A)  // Azul grisáceo: líneas aún no dibujadas
        val successColor  = Color(0xFF4ADE80)  // Verde: camino al ganar
        val failColor     = Color(0xFFFF6B6B)  // Rojo coral: camino al perder
        val playingColor  = Color(0xFF60A5FA)  // Azul claro: camino mientras juegas

        // Elegimos el color del trazo según el estado actual del juego
        val activePathColor = when (gameState) {
            GameState.WON  -> successColor
            GameState.LOST -> failColor
            else           -> playingColor
        }

        // ── PASO 1: Dibujar todas las líneas base (el esqueleto del nivel) ───
        levelData.edges.forEach { edge ->
            val a: Offset = levelData.nodes.first { it.id == edge.nodeA }.toOffset(width, height)
            val b: Offset = levelData.nodes.first { it.id == edge.nodeB }.toOffset(width, height)
            drawLine(baseLineColor, a, b, strokeW, StrokeCap.Round)
        }

        // ── PASO 2: Dibujar las aristas que el jugador ya completó ───────────
        drawnEdges.forEach { edge ->
            val a: Offset = levelData.nodes.first { it.id == edge.nodeA }.toOffset(width, height)
            val b: Offset = levelData.nodes.first { it.id == edge.nodeB }.toOffset(width, height)
            drawLine(activePathColor, a, b, strokeW, StrokeCap.Round)
        }

        // ── PASO 3: Dibujar el trazo parcial (animación en tiempo real) ──────
        // Solo se muestra mientras el jugador está arrastrando hacia un vecino
        if (gameState == GameState.PLAYING && currentNodeId != null && activeTargetId != null && activeProgress > 0f) {
            val a: Offset = levelData.nodes.first { it.id == currentNodeId }.toOffset(width, height)
            val b: Offset = levelData.nodes.first { it.id == activeTargetId }.toOffset(width, height)

            // Calculamos el punto hasta donde llega el trazo según el progreso (0.0 a 1.0)
            val partialEnd: Offset = a + ((b - a) * activeProgress)
            drawLine(playingColor, a, partialEnd, strokeW, StrokeCap.Round)
        }

        // ── PASO 4: Dibujar la X roja de fallo ───────────────────────────────
        // Se muestra durante 1 segundo (hasta que el LaunchedEffect resetea)
        if (gameState == GameState.LOST && failurePoint != null) {
            // Círculo rojo de fondo
            drawCircle(failColor, radius = 56f, center = failurePoint!!)
            // Trazo diagonal ↘
            drawLine(Color.White, failurePoint!! + Offset(-22f, -22f), failurePoint!! + Offset(22f, 22f), 14f, StrokeCap.Round)
            // Trazo diagonal ↙
            drawLine(Color.White, failurePoint!! + Offset(-22f, 22f), failurePoint!! + Offset(22f, -22f), 14f, StrokeCap.Round)
        }
    }
}