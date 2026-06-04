package com.jhonhst.upcerebrum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.jhonhst.upcerebrum.navigation.BottomNavBar
import com.jhonhst.upcerebrum.navigation.NavGraph
import com.jhonhst.upcerebrum.ui.theme.UpcerebrumTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UpcerebrumTheme() {
                val navController = rememberNavController()
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