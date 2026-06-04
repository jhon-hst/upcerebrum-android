package com.jhonhst.upcerebrum.games.oneLineDrawing

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
import kotlinx.coroutines.delay // Necesario para el delay del auto-reinicio
import kotlin.time.Duration.Companion.milliseconds

// 1. ADIÓS STRINGS HARDCODEADOS: Definimos un Enum formal para los estados
enum class GameState {
    IDLE, PLAYING, WON, LOST
}

// Función auxiliar para saber si una línea ya fue dibujada
fun hasEdge(edges: List<GameEdge>, a: Int, b: Int): Boolean {
    return edges.any { (it.nodeA == a && it.nodeB == b) || (it.nodeA == b && it.nodeB == a) }
}

@Composable
fun OneLineDrawing(levelData: LevelData) {
    // Estados principales
    var drawnEdges by remember { mutableStateOf<List<GameEdge>>(emptyList()) }
    var currentNodeId by remember { mutableStateOf<Int?>(null) }

    // Estados de animación táctil
    var activeTargetId by remember { mutableStateOf<Int?>(null) }
    var activeProgress by remember { mutableFloatStateOf(0f) }

    // Usamos el Enum en lugar de Strings
    var gameState by remember { mutableStateOf(GameState.IDLE) }
    var failurePoint by remember { mutableStateOf<Offset?>(null) }

    // Tolerancias táctiles ajustables (aumentadas a petición)
    val nodeStartTolerance = 200f // Qué tan lejos puedes tocar el inicio
    val lineDragTolerance = 300f  // Qué tan lejos puede estar el dedo de la línea mientras arrastra

    // 2. AUTO-REINICIO: Si el estado cambia a LOST, esperamos 1 seg y reseteamos
    LaunchedEffect(gameState) {
        if (gameState == GameState.LOST) {
            delay(1000L.milliseconds) // Espera 1 segundo exacto
            gameState = GameState.IDLE
            drawnEdges = emptyList()
            activeTargetId = null
            activeProgress = 0f
            failurePoint = null
            currentNodeId = null
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF243447))
            .pointerInput(levelData) {
                detectDragGestures(
                    onDragStart = { startOffset ->
                        if (gameState == GameState.WON || gameState == GameState.LOST) return@detectDragGestures

                        gameState = GameState.IDLE
                        drawnEdges = emptyList()
                        activeTargetId = null
                        activeProgress = 0f
                        failurePoint = null

                        val width = size.width
                        val height = size.height

                        val startNode = levelData.nodes.find {
                            (Offset(it.x * width, it.y * height) - startOffset).getDistance() < nodeStartTolerance
                        }

                        if (startNode != null) {
                            currentNodeId = startNode.id
                            gameState = GameState.PLAYING
                        }
                    },
                    onDrag = { change, _ ->
                        if (gameState != GameState.PLAYING || currentNodeId == null) return@detectDragGestures

                        val width = size.width
                        val height = size.height
                        val currentPos = change.position

                        val currentOffset = levelData.nodes.first { it.id == currentNodeId }.let { Offset(it.x * width, it.y * height) }

                        val validEdges = levelData.edges.filter { it.nodeA == currentNodeId || it.nodeB == currentNodeId }
                        val unvisitedNeighbors = validEdges.map { if (it.nodeA == currentNodeId) it.nodeB else it.nodeA }
                            .filter { neighborId -> !hasEdge(drawnEdges, currentNodeId!!, neighborId) }

                        var bestNeighbor: Int? = null
                        var bestProg = 0f
                        var minDist = Float.MAX_VALUE

                        for (neighborId in unvisitedNeighbors) {
                            val targetOffset = levelData.nodes.first { it.id == neighborId }.let { Offset(it.x * width, it.y * height) }
                            val lineVec = targetOffset - currentOffset
                            val touchVec = currentPos - currentOffset

                            val lineLenSq = lineVec.x * lineVec.x + lineVec.y * lineVec.y
                            if (lineLenSq > 0) {
                                val t = (touchVec.x * lineVec.x + touchVec.y * lineVec.y) / lineLenSq
                                val clampedT = t.coerceIn(0f, 1f)
                                val projection = currentOffset + (lineVec * clampedT)
                                val distToLine = (currentPos - projection).getDistance()

                                // 3. MAYOR TOLERANCIA: Permite al usuario alejar el dedo bastante
                                if (distToLine < lineDragTolerance && distToLine < minDist) {
                                    minDist = distToLine
                                    bestNeighbor = neighborId
                                    bestProg = clampedT
                                }
                            }
                        }

                        if (bestNeighbor != null) {
                            activeTargetId = bestNeighbor
                            activeProgress = bestProg

                            if (bestProg > 0.90f) {
                                drawnEdges = drawnEdges + GameEdge(currentNodeId!!, bestNeighbor)
                                currentNodeId = bestNeighbor
                                activeTargetId = null
                                activeProgress = 0f

                                if (drawnEdges.size == levelData.edges.size) {
                                    gameState = GameState.WON
                                } else {
                                    val nextValidEdges = levelData.edges.filter { it.nodeA == currentNodeId || it.nodeB == currentNodeId }
                                    val nextUnvisited = nextValidEdges.filter { edge ->
                                        !hasEdge(drawnEdges, edge.nodeA, edge.nodeB)
                                    }

                                    if (nextUnvisited.isEmpty()) {
                                        gameState = GameState.LOST
                                        failurePoint = levelData.nodes.first { it.id == currentNodeId }.let { Offset(it.x * width, it.y * height) }
                                    }
                                }
                            }
                        } else {
                            activeTargetId = null
                            activeProgress = 0f
                        }
                    },
                    onDragEnd = {
                        if (gameState == GameState.PLAYING) {
                            gameState = GameState.LOST
                            val width = size.width
                            val height = size.height

                            if (activeTargetId != null && currentNodeId != null) {
                                val currentOffset = levelData.nodes.first { it.id == currentNodeId }.let { Offset(it.x * width, it.y * height) }
                                val targetOffset = levelData.nodes.first { it.id == activeTargetId }.let { Offset(it.x * width, it.y * height) }
                                val lineVec = targetOffset - currentOffset
                                failurePoint = currentOffset + (lineVec * activeProgress)
                            } else if (currentNodeId != null) {
                                failurePoint = levelData.nodes.first { it.id == currentNodeId }.let { Offset(it.x * width, it.y * height) }
                            }
                        }
                    }
                )
            }
    ) {
        val width = size.width
        val height = size.height
        val strokeW = 32f

        val baseLineColor = Color(0xFF4A6B8A)
        val successColor = Color(0xFF4ADE80)
        val failColor = Color(0xFFFF6B6B)
        val playingColor = Color(0xFF60A5FA)

        // Usamos el Enum para determinar el color de la ruta
        val activePathColor = when (gameState) {
            GameState.WON -> successColor
            GameState.LOST -> failColor
            else -> playingColor
        }

        levelData.edges.forEach { edge ->
            val a = levelData.nodes.first { it.id == edge.nodeA }
            val b = levelData.nodes.first { it.id == edge.nodeB }
            drawLine(baseLineColor, Offset(a.x*width, a.y*height), Offset(b.x*width, b.y*height), strokeW, StrokeCap.Round)
        }

        drawnEdges.forEach { edge ->
            val a = levelData.nodes.first { it.id == edge.nodeA }
            val b = levelData.nodes.first { it.id == edge.nodeB }
            drawLine(activePathColor, Offset(a.x*width, a.y*height), Offset(b.x*width, b.y*height), strokeW, StrokeCap.Round)
        }

        if (gameState == GameState.PLAYING && currentNodeId != null && activeTargetId != null && activeProgress > 0f) {
            val a = levelData.nodes.first { it.id == currentNodeId }
            val b = levelData.nodes.first { it.id == activeTargetId }
            val start = Offset(a.x*width, a.y*height)
            val end = Offset(b.x*width, b.y*height)

            val currentFill = start + ((end - start) * activeProgress)
            drawLine(playingColor, start, currentFill, strokeW, StrokeCap.Round)
        }

        if (gameState == GameState.LOST && failurePoint != null) {
            drawCircle(failColor, radius = 56f, center = failurePoint!!)
            drawLine(Color.White, failurePoint!! + Offset(-22f,-22f), failurePoint!! + Offset(22f,22f), 14f, StrokeCap.Round)
            drawLine(Color.White, failurePoint!! + Offset(-22f,22f), failurePoint!! + Offset(22f,-22f), 14f, StrokeCap.Round)
        }
    }
}