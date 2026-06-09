package com.jhonhst.upcerebrum.navigation

import com.jhonhst.upcerebrum.R
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import com.jhonhst.upcerebrum.ui.theme.AreaGreen
import com.jhonhst.upcerebrum.ui.theme.CerebrumSurface
import com.jhonhst.upcerebrum.ui.theme.TextPrimary
import com.jhonhst.upcerebrum.ui.theme.TextSecondary

private data class NavItem(
    val route: Route,
    val label: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

private val items = listOf(
    NavItem(Route.Home,     R.string.nav_home,     Icons.Outlined.Home),
    NavItem(Route.Path,     R.string.nav_path,     Icons.Outlined.Route),
    NavItem(Route.Settings, R.string.nav_settings, Icons.Outlined.Settings),
)

@Composable
fun BottomNavBar(navController: NavController) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentDest = backStack?.destination

    Column {
        // 1. Línea blanca que separa el menú del resto de la aplicación
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = TextPrimary // Usamos tu color blanco definido en Color.kt
        )

        NavigationBar(
            containerColor = CerebrumSurface, // Color de fondo del menú
            modifier = Modifier.height(80.dp)  // Altura estándar recomendada
        ) {
            items.forEachIndexed { index, item ->
                val selected = currentDest?.hasRoute(item.route::class) == true
                val translatedLabel = stringResource(id = item.label)

                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = translatedLabel) },
                        label = { Text(translatedLabel, style = MaterialTheme.typography.bodyLarge) },
                        colors = NavigationBarItemDefaults.colors(
                            // Cuando está activo: Icono y texto se pintan con tu color secundario (AreaGreen)
                            selectedIconColor = AreaGreen,
                            selectedTextColor = AreaGreen,
                            // Cuando está inactivo: Icono y texto se quedan en blanco
                            unselectedIconColor = TextPrimary,
                            unselectedTextColor = TextPrimary,
                            // El "indicador" es la pastilla de fondo que Material 3 pone detrás del icono activo.
                            // Lo dejamos transparente para que no opaque tu diseño limpio de colores.
                            indicatorColor = androidx.compose.ui.graphics.Color.Transparent
                        )
                    )

                    // 2. Línea blanca que separa cada botón del menú (excepto el último) al 100% de altura
                    if (index < items.lastIndex) {
                        VerticalDivider(
                            modifier = Modifier
                                .fillMaxHeight() // Ocupa todo el alto disponible del Row padre
                                .width(1.dp),
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}