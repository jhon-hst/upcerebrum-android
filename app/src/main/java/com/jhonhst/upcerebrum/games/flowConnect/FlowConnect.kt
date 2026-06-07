package com.jhonhst.upcerebrum.games.flowConnect

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.abs

// ─── MODELOS DE DATOS ────────────────────────────────────────────────────────

data class GridPoint(val x: Int, val y: Int)
data class Dot(val point: GridPoint, val id: Int, val color: Color)
data class FlowLevelData(val columns: Int, val rows: Int, val dots: List<Dot>)

// ─── ESTADO DEL JUEGO ────────────────────────────────────────────────────────

enum class GameState { PLAYING, WON }

@Composable
fun FlowConnect(levelData: FlowLevelData) {

    // ── ESTADOS DEL JUEGO ──
    var paths by remember { mutableStateOf<Map<Int, List<GridPoint>>>(emptyMap()) }
    var activePathId by remember { mutableStateOf<Int?>(null) }
    var gameState by remember { mutableStateOf(GameState.PLAYING) }

    // ── CONFIGURACIÓN DE DISEÑO ──
    val boardColor = Color(0xFF1E2D3D) // Fondo general del juego
    val emptyCellColor = Color(0xFF2A3F54) // Color de las celdas vacías

    // Obtenemos los valores en píxeles según la densidad de la pantalla
    val density = LocalDensity.current
    val screenPaddingPx = with(density) { 24.dp.toPx() } // Padding lateral del celular
    val cellGapPx = with(density) { 6.dp.toPx() } // Espacio entre las cuadrículas

    // ── CANVAS PRINCIPAL ──
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(boardColor)
            .pointerInput(levelData) {
                // Usamos un control táctil de bajo nivel para una respuesta instantánea
                awaitPointerEventScope {
                    while (true) {
                        val downEvent = awaitFirstDown()
                        if (gameState == GameState.WON) continue

                        // 1. CÁLCULO DE DIMENSIONES (Igual que en el bloque de dibujo)
                        val availableW = size.width - (screenPaddingPx * 2)
                        val availableH = size.height - (screenPaddingPx * 2)
                        val maxCellW = availableW / levelData.columns
                        val maxCellH = availableH / levelData.rows
                        val cellSize = minOf(maxCellW, maxCellH) // Garantiza celdas CUADRADAS

                        val gridWidth = cellSize * levelData.columns
                        val gridHeight = cellSize * levelData.rows
                        val startX = (size.width - gridWidth) / 2f // Centrado X
                        val startY = (size.height - gridHeight) / 2f // Centrado Y

                        // 2. IDENTIFICAR QUÉ CELDA SE TOCÓ (ON DOWN)
                        val relX = downEvent.position.x - startX
                        val relY = downEvent.position.y - startY

                        // Si tocó fuera del tablero, ignoramos
                        if (relX < 0 || relY < 0 || relX >= gridWidth || relY >= gridHeight) continue

                        val startGridX = (relX / cellSize).toInt().coerceIn(0, levelData.columns - 1)
                        val startGridY = (relY / cellSize).toInt().coerceIn(0, levelData.rows - 1)
                        val touchedPoint = GridPoint(startGridX, startGridY)

                        // ¿Tocó un punto base? -> Reseteamos la línea instantáneamente
                        val touchedDot = levelData.dots.find { it.point == touchedPoint }
                        if (touchedDot != null) {
                            activePathId = touchedDot.id
                            val newPaths = paths.toMutableMap()
                            newPaths[touchedDot.id] = listOf(touchedPoint)
                            paths = newPaths
                        } else {
                            // ¿Tocó una línea existente? -> La cortamos hasta ese punto
                            var foundPath = false
                            for ((id, path) in paths) {
                                if (path.contains(touchedPoint)) {
                                    activePathId = id
                                    val index = path.indexOf(touchedPoint)
                                    val newPaths = paths.toMutableMap()
                                    newPaths[id] = path.take(index + 1)
                                    paths = newPaths
                                    foundPath = true
                                    break
                                }
                            }
                            if (!foundPath) activePathId = null
                        }

                        // 3. BUCLE DE ARRASTRE (ON DRAG)
                        val pointerId = downEvent.id
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.find { it.id == pointerId }

                            // Si el jugador levanta el dedo
                            if (change == null || !change.pressed) {
                                activePathId = null
                                break
                            }

                            if (activePathId == null) continue

                            val moveRelX = change.position.x - startX
                            val moveRelY = change.position.y - startY

                            // Evitar salir de los límites
                            if (moveRelX < 0 || moveRelY < 0 || moveRelX >= gridWidth || moveRelY >= gridHeight) continue

                            val gridX = (moveRelX / cellSize).toInt().coerceIn(0, levelData.columns - 1)
                            val gridY = (moveRelY / cellSize).toInt().coerceIn(0, levelData.rows - 1)
                            val currentPoint = GridPoint(gridX, gridY)

                            val currentPath = paths[activePathId!!] ?: continue
                            val lastPoint = currentPath.last()

                            if (currentPoint != lastPoint) {
                                // Validar movimiento ortogonal (horizontal o vertical)
                                val isAdjacent = (abs(currentPoint.x - lastPoint.x) + abs(currentPoint.y - lastPoint.y)) == 1

                                if (isAdjacent) {
                                    if (currentPath.size > 1 && currentPath[currentPath.size - 2] == currentPoint) {
                                        // Retroceso
                                        val updatedPaths = paths.toMutableMap()
                                        updatedPaths[activePathId!!] = currentPath.dropLast(1)
                                        paths = updatedPaths
                                    } else {
                                        // Avance
                                        val dotAtPoint = levelData.dots.find { it.point == currentPoint }
                                        if (dotAtPoint != null && dotAtPoint.id != activePathId) continue

                                        val isAlreadyAtTarget = levelData.dots.any { it.point == lastPoint && it.id == activePathId && currentPath.size > 1 }
                                        if (isAlreadyAtTarget) continue

                                        var newPath = currentPath + currentPoint
                                        val updatedPaths = paths.toMutableMap()

                                        // Cortar líneas enemigas
                                        for ((id, path) in updatedPaths) {
                                            if (id != activePathId && path.contains(currentPoint)) {
                                                val index = path.indexOf(currentPoint)
                                                updatedPaths[id] = path.take(index)
                                            }
                                        }

                                        // Evitar bucles propios
                                        if (currentPath.contains(currentPoint)) {
                                            val index = currentPath.indexOf(currentPoint)
                                            newPath = currentPath.take(index + 1)
                                        }

                                        updatedPaths[activePathId!!] = newPath
                                        paths = updatedPaths

                                        // ── REVISIÓN DE VICTORIA ──
                                        var allConnected = true
                                        for ((id, dots) in levelData.dots.groupBy { it.id }) {
                                            val p = paths[id] ?: emptyList()
                                            if (p.isEmpty() || p.first() != dots[0].point || p.last() != dots[1].point) {
                                                allConnected = false
                                                break
                                            }
                                        }

                                        val totalCells = levelData.columns * levelData.rows
                                        val filledCells = paths.values.flatten().distinct().size
                                        if (allConnected && filledCells == totalCells) {
                                            gameState = GameState.WON
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
    ) {
        // 1. CÁLCULO DE DIMENSIONES Y CENTRADO PARA EL DIBUJO
        val availableW = size.width - (screenPaddingPx * 2)
        val availableH = size.height - (screenPaddingPx * 2)
        val maxCellW = availableW / levelData.columns
        val maxCellH = availableH / levelData.rows
        val cellSize = minOf(maxCellW, maxCellH)

        val gridWidth = cellSize * levelData.columns
        val gridHeight = cellSize * levelData.rows
        val startX = (size.width - gridWidth) / 2f
        val startY = (size.height - gridHeight) / 2f

        val actualCellSize = cellSize - cellGapPx // Tamaño visual de la celda restando el espacio

        // 2. DIBUJAR CELDAS (EL TABLERO CON GAPS)
        for (x in 0 until levelData.columns) {
            for (y in 0 until levelData.rows) {
                val cellLeft = startX + (x * cellSize) + (cellGapPx / 2f)
                val cellTop = startY + (y * cellSize) + (cellGapPx / 2f)

                // Buscar si la celda actual es parte de algún camino
                val pathOwnerId = paths.entries.find { it.value.contains(GridPoint(x, y)) }?.key

                // Si la celda tiene un camino, se pinta de un tono suave de ese color, si no, azul oscuro
                val cellColor = if (pathOwnerId != null) {
                    levelData.dots.first { it.id == pathOwnerId }.color.copy(alpha = 0.25f)
                } else {
                    emptyCellColor
                }

                drawRoundRect(
                    color = cellColor,
                    topLeft = Offset(cellLeft, cellTop),
                    size = Size(actualCellSize, actualCellSize),
                    cornerRadius = CornerRadius(12f) // Bordes redondeados para que se vea moderno
                )
            }
        }

        // 3. DIBUJAR LAS LÍNEAS DE CONEXIÓN
        for ((id, path) in paths) {
            val pathColor = levelData.dots.first { it.id == id }.color

            if (path.size > 1) {
                val pathStroke = androidx.compose.ui.graphics.Path()

                // Calculamos el centro real de la primera celda
                val startCenterX = startX + (path.first().x * cellSize) + (cellSize / 2f)
                val startCenterY = startY + (path.first().y * cellSize) + (cellSize / 2f)
                pathStroke.moveTo(startCenterX, startCenterY)

                for (i in 1 until path.size) {
                    val centerX = startX + (path[i].x * cellSize) + (cellSize / 2f)
                    val centerY = startY + (path[i].y * cellSize) + (cellSize / 2f)
                    pathStroke.lineTo(centerX, centerY)
                }

                drawPath(
                    path = pathStroke,
                    color = pathColor,
                    style = Stroke(
                        width = cellSize * 0.35f, // Grosor dinámico de la tubería
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }

        // 4. DIBUJAR LOS PUNTOS BASE (DOTS)
        levelData.dots.forEach { dot ->
            val centerX = startX + (dot.point.x * cellSize) + (cellSize / 2f)
            val centerY = startY + (dot.point.y * cellSize) + (cellSize / 2f)
            drawCircle(
                color = dot.color,
                radius = cellSize * 0.35f, // Círculo grande que tapa el inicio de la línea
                center = Offset(centerX, centerY)
            )
        }

        // 5. OVERLAY DE VICTORIA
        if (gameState == GameState.WON) {
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.5f),
                size = size
            )
        }
    }
}