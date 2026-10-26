package com.example.turkids.model

enum class IdiomaPractica(
    val etiqueta: String,
    val bandera: String,
    val descripcion: String
) {
    TURCO(
        etiqueta = "Turco",
        bandera = "\uD83C\uDDF9\uD83C\uDDF7",
        descripcion = "Verás palabras en turco y responderás en español"
    ),
    ESPANOL(
        etiqueta = "Español",
        bandera = "\uD83C\uDDEA\uD83C\uDDF8",
        descripcion = "Verás palabras en español y responderás en turco"
    )
}
