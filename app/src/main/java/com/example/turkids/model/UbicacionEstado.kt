package com.example.turkids.model

sealed interface UbicacionEstado {
    data object Verificando : UbicacionEstado
    data object NoEnUso : UbicacionEstado
    data class EnUso(val lugar: String) : UbicacionEstado
}

fun UbicacionEstado.texto(): String = when (this) {
    is UbicacionEstado.EnUso -> "Ubicación actual: $lugar"
    UbicacionEstado.Verificando -> "Obteniendo ubicación..."
    UbicacionEstado.NoEnUso -> "Tu ubicación no está en uso"
}

// Por qué la ubicación no está en uso; la interfaz ofrece una acción distinta para cada caso
enum class ProblemaUbicacion {
    NINGUNO,
    // Permiso rechazado, pero el sistema todavía permite volver a pedirlo
    PERMISO_DENEGADO,
    // Permiso rechazado sin posibilidad de volver a preguntar: solo se puede cambiar en Ajustes
    PERMISO_BLOQUEADO,
    UBICACION_DESACTIVADA
}
