package com.jhonhst.upcerebrum.views.path.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun ProgressPath(
    totalLevels: Int = 50,
    currentLevel: Int = 12 // Nivel desbloqueado
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        reverseLayout = true, // Mantenemos el mapa de abajo hacia arriba
        contentPadding = PaddingValues(vertical = 48.dp)
    ) {
        items(totalLevels) { index ->
            val levelNumber = index + 1
            val isUnlocked = levelNumber <= currentLevel
            val isCurrent = levelNumber == currentLevel

            // Curva del nodo actual (sin cambios)
            val curveFactor = sin(index * 0.7)
            val xOffset = (curveFactor * 100).dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    // --- LÓGICA DE DIBUJO CORREGIDA ---
                    .drawBehind {
                        // Ahora conectamos el nivel actual (N) con el nivel SIGUIENTE (N+1)
                        // Dejamos de dibujar cuando llegamos al penúltimo nivel.
                        if (index < totalLevels - 1) {
                            val nextIndex = index + 1
                            val nextCurveFactor = sin(nextIndex * 0.7)
                            val nextXOffsetPx = (nextCurveFactor * 100).dp.toPx()
                            val currentXOffsetPx = xOffset.toPx()

                            // La línea se marca como "completada" si el destino está desbloqueado
                            val lineUnlocked = (nextIndex + 1) <= currentLevel
                            val lineColor = if (lineUnlocked) Color(0xFF81C784) else Color(0xFFE0E0E0)

                            drawLine(
                                color = lineColor,
                                // Punto de inicio: centro del ítem actual (index)
                                start = Offset(center.x + currentXOffsetPx, center.y),
                                // Punto final: centro del ítem SIGUIENTE (visually ABOVE)
                                // Restamos size.height a 'y' porque en las coordenadas del canvas, 'y' disminuye hacia arriba.
                                end = Offset(center.x + nextXOffsetPx, center.y - size.height),
                                strokeWidth = 30f,
                                cap = StrokeCap.Round
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // UI DEL NODO (Círculo) -> Esto se dibuja DESPUÉS de drawBehind
                Box(
                    modifier = Modifier
                        .offset(x = xOffset)
                        .size(if (isCurrent) 80.dp else 64.dp)
                        .clip(CircleShape)
                        .background(
                            if (isUnlocked) Color(0xFF4CAF50) else Color(0xFFBDBDBD)
                        )
                        .border(
                            width = if (isCurrent) 6.dp else 4.dp,
                            color = if (isUnlocked) Color(0xFF2E7D32) else Color(0xFF9E9E9E),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = levelNumber.toString(),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}