package com.jhonhst.upcerebrum.views.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.jhonhst.upcerebrum.navigation.Route

@Composable
fun SettingsView(  navController: NavHostController,) {
    Text(
        text = "Hello I am Settings"
    )

    Button (onClick = {navController.navigate(Route.Profile)}) {
        Text("Ir a Profile")
    }
}