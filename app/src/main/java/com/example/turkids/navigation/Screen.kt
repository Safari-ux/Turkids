package com.example.turkids.navigation

sealed class Screen(val route: String) {
    data object Practica : Screen(route = "practica")
    data object Home : Screen(route = "home")
    data object AcercaDe : Screen(route = "acerca_de")
    data object Registro : Screen(route = "registro")
    data object ResumenRegistro : Screen(route = "resumen_registro")

    data class Leccion(val unidadId: String) : Screen(route = "leccion/$unidadId") {
        companion object {
            const val ROUTE_PATTERN = "leccion/{unidadId}"
            const val ARG_UNIDAD_ID = "unidadId"
        }
    }

    data class Resumen(val aciertos: Int, val total: Int) :
        Screen(route = "resumen/$aciertos/$total") {
        companion object {
            const val ROUTE_PATTERN = "resumen/{aciertos}/{total}"
            const val ARG_ACIERTOS = "aciertos"
            const val ARG_TOTAL = "total"
        }
    }
}