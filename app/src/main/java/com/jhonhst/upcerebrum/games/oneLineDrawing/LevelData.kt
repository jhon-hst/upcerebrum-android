package com.jhonhst.upcerebrum.games.oneLineDrawing

// Representa un punto en la pantalla. x e y van de 0.0 a 1.0
data class GameNode(val id: Int, val x: Float, val y: Float)

// Representa una línea que conecta dos nodos
data class GameEdge(val nodeA: Int, val nodeB: Int)

// El modelo principal que recibirás de tu JSON
data class LevelData(
    val nodes: List<GameNode>,
    val edges: List<GameEdge>
)