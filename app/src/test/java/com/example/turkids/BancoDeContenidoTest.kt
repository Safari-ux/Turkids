package com.example.turkids

import com.example.turkids.model.BancoDeContenido
import com.example.turkids.model.IdiomaPractica
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BancoDeContenidoTest {

    private val semillas = listOf(0L, 1L, 42L, 12345L, 1_700_000_000_000L)

    private fun combinaciones() = BancoDeContenido.unidades.flatMap { unidad ->
        IdiomaPractica.entries.flatMap { idioma ->
            semillas.map { semilla -> Triple(unidad, idioma, semilla) }
        }
    }

    @Test
    fun generaUnaPreguntaPorPalabra() {
        combinaciones().forEach { (unidad, idioma, semilla) ->
            val preguntas = BancoDeContenido.generarPreguntas(unidad, idioma, semilla)
            assertEquals(unidad.palabras.size, preguntas.size)
            assertEquals(unidad.palabras, preguntas.map { it.palabra })
        }
    }

    @Test
    fun cadaPreguntaTieneTresOpciones() {
        combinaciones().forEach { (unidad, idioma, semilla) ->
            BancoDeContenido.generarPreguntas(unidad, idioma, semilla).forEach {
                assertEquals("Unidad ${unidad.id}", 3, it.opciones.size)
            }
        }
    }

    @Test
    fun lasOpcionesNoSeRepitenYIncluyenLaCorrecta() {
        combinaciones().forEach { (unidad, idioma, semilla) ->
            BancoDeContenido.generarPreguntas(unidad, idioma, semilla).forEach {
                assertEquals(it.opciones.size, it.opciones.toSet().size)
                assertTrue(it.respuestaCorrecta in it.opciones)
            }
        }
    }

    @Test
    fun lasOpcionesSonDeLaMismaUnidad() {
        combinaciones().forEach { (unidad, idioma, semilla) ->
            val textosDeLaUnidad = unidad.palabras.flatMap { listOf(it.es, it.tr) }.toSet()
            BancoDeContenido.generarPreguntas(unidad, idioma, semilla).forEach { pregunta ->
                assertTrue(
                    "Opciones fuera de la unidad ${unidad.id}: ${pregunta.opciones}",
                    textosDeLaUnidad.containsAll(pregunta.opciones)
                )
            }
        }
    }

    @Test
    fun todasLasOpcionesEstanEnElIdiomaDeRespuesta() {
        combinaciones().forEach { (unidad, idioma, semilla) ->
            val esperadas = unidad.palabras.map { if (idioma == IdiomaPractica.TURCO) it.es else it.tr }.toSet()
            BancoDeContenido.generarPreguntas(unidad, idioma, semilla).forEach { pregunta ->
                assertEquals(idioma == IdiomaPractica.TURCO, pregunta.mostrarTurco)
                assertTrue(
                    "Se mezclaron idiomas en ${unidad.id}: ${pregunta.opciones}",
                    esperadas.containsAll(pregunta.opciones)
                )
            }
        }
    }

    @Test
    fun lasPreguntasNuncaMezclanIdiomasEntreSi() {
        BancoDeContenido.unidades.forEach { unidad ->
            IdiomaPractica.entries.forEach { idioma ->
                val preguntas = BancoDeContenido.generarPreguntas(unidad, idioma, semilla = 7L)
                assertEquals(1, preguntas.map { it.mostrarTurco }.toSet().size)
            }
        }
    }

    @Test
    fun laMismaSemillaReproduceLaMismaLeccion() {
        BancoDeContenido.unidades.forEach { unidad ->
            IdiomaPractica.entries.forEach { idioma ->
                val a = BancoDeContenido.generarPreguntas(unidad, idioma, semilla = 99L)
                val b = BancoDeContenido.generarPreguntas(unidad, idioma, semilla = 99L)
                assertEquals(a, b)
            }
        }
    }
}
