package com.jhonhst.upcerebrum.views.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.jhonhst.upcerebrum.games.oneLineDrawing.GameEdge
import com.jhonhst.upcerebrum.games.oneLineDrawing.GameNode
import com.jhonhst.upcerebrum.games.oneLineDrawing.LevelData
import com.jhonhst.upcerebrum.games.oneLineDrawing.OneLineDrawing

@Composable
fun FullVerticalCarousel() {
    // 1. Definimos la cantidad total de "pantallas"
    val pageCount = 10

    // 2. Creamos el estado del Pager para controlar el scroll
    val pagerState = rememberPagerState(pageCount = { pageCount })

    val levelHouse = LevelData(
        nodes = listOf(
            GameNode(1, 0.5f, 0.2f), // Techo (Punta)
            GameNode(2, 0.2f, 0.4f), // Esquina Superior Izquierda
            GameNode(3, 0.8f, 0.4f), // Esquina Superior Derecha
            GameNode(4, 0.2f, 0.8f), // Esquina Inferior Izquierda
            GameNode(5, 0.8f, 0.8f)  // Esquina Inferior Derecha
        ),
        edges = listOf(
            // Techo
            GameEdge(1, 2), GameEdge(1, 3), GameEdge(2, 3),
            // Paredes y base
            GameEdge(2, 4), GameEdge(3, 5), GameEdge(4, 5),
            // Cruce interior (la X dentro de la casa)
            GameEdge(2, 5), GameEdge(3, 4)
        )
    )

    val levelSquare = LevelData(
        nodes = listOf(
            GameNode(1, 0.2f, 0.3f), // Arriba Izquierda
            GameNode(2, 0.8f, 0.3f), // Arriba Derecha
            GameNode(3, 0.2f, 0.7f), // Abajo Izquierda
            GameNode(4, 0.8f, 0.7f)  // Abajo Derecha
        ),
        edges = listOf(
            GameEdge(1, 2), // Borde superior
            GameEdge(2, 4), // Borde derecho
            GameEdge(4, 3), // Borde inferior
            GameEdge(3, 1), // Borde izquierdo
            GameEdge(1, 4)  // Diagonal
        )
    )

    val levelChristmasTree = LevelData(
        nodes = listOf(
            // --- TRIÁNGULO SUPERIOR ---
            GameNode(1, 0.5f, 0.15f),  // Punta del árbol
            GameNode(2, 0.35f, 0.35f), // Esquina inferior izquierda (Nivel 1)
            GameNode(3, 0.65f, 0.35f), // Esquina inferior derecha (Nivel 1)
            GameNode(4, 0.5f, 0.35f),  // Centro de la base (Nivel 1) - Conecta hacia abajo

            // --- TRIÁNGULO MEDIO ---
            GameNode(5, 0.25f, 0.55f), // Esquina inferior izquierda (Nivel 2)
            GameNode(6, 0.75f, 0.55f), // Esquina inferior derecha (Nivel 2)
            GameNode(7, 0.5f, 0.55f),  // Centro de la base (Nivel 2) - ¡Aquí te salió la X roja!

            // --- TRIÁNGULO INFERIOR Y TRONCO ---
            GameNode(8, 0.15f, 0.75f),  // Esquina inferior izquierda (Nivel 3)
            GameNode(9, 0.85f, 0.75f),  // Esquina inferior derecha (Nivel 3)
            GameNode(10, 0.42f, 0.75f), // Tronco (Esquina superior izquierda)
            GameNode(11, 0.58f, 0.75f), // Tronco (Esquina superior derecha)
            GameNode(12, 0.42f, 0.9f),  // Tronco (Esquina inferior izquierda)
            GameNode(13, 0.58f, 0.9f)   // Tronco (Esquina inferior derecha)
        ),
        edges = listOf(
            // --- LÍNEAS DEL TRIÁNGULO SUPERIOR ---
            GameEdge(1, 2), // Techo diagonal izquierdo
            GameEdge(1, 3), // Techo diagonal derecho
            GameEdge(2, 4), // Mitad izquierda de la base
            GameEdge(4, 3), // Mitad derecha de la base

            // --- LÍNEAS DEL TRIÁNGULO MEDIO ---
            // Nacen desde el nodo central (4) de la base de arriba
            GameEdge(4, 5), // Techo diagonal izquierdo
            GameEdge(4, 6), // Techo diagonal derecho
            GameEdge(5, 7), // Mitad izquierda de la base
            GameEdge(7, 6), // Mitad derecha de la base

            // --- LÍNEAS DEL TRIÁNGULO INFERIOR ---
            // Nacen desde el nodo central (7) de la base de arriba
            GameEdge(7, 8),  // Techo diagonal izquierdo (la línea roja de tu captura)
            GameEdge(7, 9),  // Techo diagonal derecho
            GameEdge(8, 10), // Base izquierda hacia el tronco
            GameEdge(9, 11), // Base derecha hacia el tronco

            // --- LÍNEAS DEL TRONCO ---
            GameEdge(10, 12), // Pared izquierda del tronco
            GameEdge(11, 13), // Pared derecha del tronco
            GameEdge(12, 13)  // Piso del tronco
        )
    )

    // 3. VerticalPager maneja el scroll estilo TikTok automáticamente
    VerticalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize() // Asegura que el carrusel tome toda la pantalla
    ) { page ->

        // 4. Contenido de cada pantalla individual
        Box(
            modifier = Modifier
                .fillMaxSize() // Cada página toma toda la pantalla
                // Agregamos un fondo intercalado solo para que notes visualmente el salto al hacer scroll
                .background(if (page % 2 == 0) Color.DarkGray else Color.Gray),
            contentAlignment = Alignment.Center // Centra el contenido en la pantalla
        ) {
            // El texto que solicitaste
            OneLineDrawing(levelChristmasTree)
        }
    }
}