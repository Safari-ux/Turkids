package com.example.turkids.model

// Perfiles ficticios de prueba, sin datos personales reales
data class PerfilPrueba(val id: String, val nombre: String)

object PerfilesPrueba {
    val lista = listOf(
        PerfilPrueba(id = "perfil-prueba-1", nombre = "Prueba 1"),
        PerfilPrueba(id = "perfil-prueba-2", nombre = "Prueba 2")
    )
}

data class ProgresoUiState(
    val perfilActivoId: String = PerfilesPrueba.lista.first().id,
    val idioma: IdiomaPractica = IdiomaPractica.TURCO,
    val estrellas: Map<String, Int> = emptyMap(),
    // true mientras DataStore entrega la primera lectura; evita mostrar valores por defecto
    val cargando: Boolean = false
)

data class LeccionGuardada(
    val unidadId: String,
    val idioma: IdiomaPractica,
    val semilla: Long,
    val indice: Int,
    val aciertos: Int
)

fun calcularEstrellas(aciertos: Int, total: Int): Int = when {
    total == 0 -> 0
    aciertos == total -> 3
    aciertos >= total / 2 -> 2
    else -> 1
}