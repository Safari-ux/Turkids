package com.example.turkids.model

import kotlin.random.Random

// Contenido ficticio de prueba: las unidades y su orden se ajustan según el especialista
object BancoDeContenido {

    val unidades = listOf(
        Unidad(
            id = "saludos",
            titulo = "Saludos",
            emoji = "\uD83D\uDC4B",
            palabras = listOf(
                Palabra(es = "Hola", tr = "Merhaba", emoji = "\uD83D\uDC4B"),
                Palabra(es = "Gracias", tr = "Teşekkürler", emoji = "\uD83D\uDE4F"),
                Palabra(es = "Sí", tr = "Evet", emoji = "\u2705"),
                Palabra(es = "No", tr = "Hayır", emoji = "\u274C")
            )
        ),
        Unidad(
            id = "animales",
            titulo = "Animales",
            emoji = "\uD83D\uDC36",
            palabras = listOf(
                Palabra(es = "Gato", tr = "Kedi", emoji = "\uD83D\uDC31"),
                Palabra(es = "Perro", tr = "Köpek", emoji = "\uD83D\uDC36"),
                Palabra(es = "Pájaro", tr = "Kuş", emoji = "\uD83D\uDC26"),
                Palabra(es = "Pez", tr = "Balık", emoji = "\uD83D\uDC1F")
            )
        ),
        Unidad(
            id = "numeros",
            titulo = "Números",
            emoji = "\uD83D\uDD22",
            palabras = listOf(
                Palabra(es = "Uno", tr = "Bir", emoji = "1\uFE0F\u20E3"),
                Palabra(es = "Dos", tr = "İki", emoji = "2\uFE0F\u20E3"),
                Palabra(es = "Tres", tr = "Üç", emoji = "3\uFE0F\u20E3"),
                Palabra(es = "Cuatro", tr = "Dört", emoji = "4\uFE0F\u20E3")
            )
        )
    )

    // Todas las preguntas van en el idioma elegido; la misma semilla reproduce la misma lección
    fun generarPreguntas(unidad: Unidad, idioma: IdiomaPractica, semilla: Long): List<Pregunta> {
        val azar = Random(semilla)
        val mostrarTurco = idioma == IdiomaPractica.TURCO

        return unidad.palabras.map { palabra ->
            val correcta = if (mostrarTurco) palabra.es else palabra.tr

            val distractores = unidad.palabras
                .filter { it != palabra }
                .map { if (mostrarTurco) it.es else it.tr }
                .shuffled(azar)
                .take(2)

            Pregunta(
                palabra = palabra,
                opciones = (distractores + correcta).shuffled(azar),
                mostrarTurco = mostrarTurco
            )
        }
    }
}
