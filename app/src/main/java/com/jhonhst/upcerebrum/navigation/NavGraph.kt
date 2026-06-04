package com.jhonhst.upcerebrum.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jhonhst.upcerebrum.views.home.HomeView
import com.jhonhst.upcerebrum.views.path.PathView
import com.jhonhst.upcerebrum.views.profile.ProfileView
import com.jhonhst.upcerebrum.views.settings.SettingsView

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController    = navController,
        startDestination = Route.Home,
        modifier         = modifier,
    ) {
        composable<Route.Home>     { HomeView() }
        composable<Route.Path>     { PathView() }
        composable<Route.Settings> { SettingsView(navController) }
        composable<Route.Profile>  { ProfileView() }
    }
}