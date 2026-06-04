package com.jhonhst.upcerebrum.navigation
import kotlinx.serialization.Serializable

@Serializable sealed class Route {

    @Serializable data object Profile     : Route()
    @Serializable data object Home     : Route()
    @Serializable data object Path     : Route()
    @Serializable data object Settings : Route()
}
