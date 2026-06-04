package com.jhonhst.upcerebrum.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.getValue

private data class NavItem(
    val route: Route,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

private val items = listOf(
    NavItem(Route.Home,     "Home",     Icons.Outlined.Home),
    NavItem(Route.Path,     "Path",     Icons.Outlined.Route),
    NavItem(Route.Settings, "Settings", Icons.Outlined.Settings),
)

@Composable
fun BottomNavBar(navController: NavController) {
    val backStack  by navController.currentBackStackEntryAsState()
    val currentDest = backStack?.destination

    NavigationBar {
        items.forEach { item ->
            val selected = currentDest?.hasRoute(item.route::class) == true
            NavigationBarItem(
                selected = selected,
                onClick  = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState    = true
                    }
                },
                icon  = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
            )
        }
    }
}