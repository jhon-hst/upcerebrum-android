package com.jhonhst.upcerebrum.games.oneLineDrawing
import androidx.compose.ui.geometry.Offset

// Representa un punto en la pantalla.
// x e y van de 0.0 a 1.0 (porcentaje de la pantalla), así el nivel se adapta a cualquier tamaño.
data class GameNode(val id: Int, val x: Float, val y: Float)

// Representa una línea que conecta dos nodos.
// nodeA y nodeB son los IDs de los nodos que une.
data class GameEdge(val nodeA: Int, val nodeB: Int)

// El modelo principal que describe un nivel completo.
// nodes = todos los puntos del dibujo
// edges = todas las líneas que los conectan
data class LevelData(
    val nodes: List<GameNode>,
    val edges: List<GameEdge>
)

// ─── EXTENSIÓN ÚTIL ──────────────────────────────────────────────────────────
// En lugar de repetir este cálculo 10+ veces en el Canvas,
// le "enseñamos" a GameNode a convertirse solo en un Offset de pantalla.
// Uso: miNodo.toOffset(width, height)

fun GameNode.toOffset(width: Float, height: Float) = Offset(x * width, y * height)