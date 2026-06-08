package com.jhonhst.upcerebrum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.jhonhst.upcerebrum.navigation.BottomNavBar
import com.jhonhst.upcerebrum.navigation.NavGraph
import com.jhonhst.upcerebrum.ui.theme.UpcerebrumTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge es obligatorio en API 35
        enableEdgeToEdge()

        setContent {
            UpcerebrumTheme {
                val view = LocalView.current
                // Forzamos iconos claros (blancos) en la barra de estado
                if (!view.isInEditMode) {
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                }

                val navController = rememberNavController()

                // Contenedor principal para "pintar" la barra de estado manualmente
                Column(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        bottomBar = { BottomNavBar(navController) }
                    ) { innerPadding ->
                        NavGraph(
                            navController = navController,
                            modifier      = Modifier.padding(innerPadding),
                        )
                    }
                }
            }
        }
    }
}