package com.example.turkids.model

data class Palabra(
    val es: String,
    val tr: String,
    val emoji: String
)

data class Unidad(
    val id: String,
    val titulo: String,
    val emoji: String,
    val palabras: List<Palabra>
)

// mostrarTurco = true: se muestra la palabra en turco y se responde en español
data class Pregunta(
    val palabra: Palabra,
    val opciones: List<String>,
    val mostrarTurco: Boolean
) {
    val respuestaCorrecta: String
        get() = if (mostrarTurco) palabra.es else palabra.tr
}
