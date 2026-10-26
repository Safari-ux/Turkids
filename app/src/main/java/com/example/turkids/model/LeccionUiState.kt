package com.example.turkids.model

data class LeccionUiState(
    val tituloUnidad: String = "",
    val preguntas: List<Pregunta> = emptyList(),
    val indiceActual: Int = 0,
    val aciertos: Int = 0,
    val opcionSeleccionada: String? = null,
    val descartadas: Set<String> = emptySet(),
    val intentosFallidos: Int = 0,
    val respondido: Boolean = false,
    val esCorrecta: Boolean = false,
    val finalizado: Boolean = false
) {
    val preguntaActual: Pregunta?
        get() = preguntas.getOrNull(indiceActual)

    val progreso: Float
        get() = if (preguntas.isEmpty()) 0f else (indiceActual.toFloat() / preguntas.size)

    val totalPreguntas: Int
        get() = preguntas.size
}