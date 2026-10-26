package com.example.turkids

import com.example.turkids.model.calcularEstrellas
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgresoTest {

    @Test
    fun sinPreguntasNoHayEstrellas() {
        assertEquals(0, calcularEstrellas(aciertos = 0, total = 0))
    }

    @Test
    fun todasCorrectasDanTresEstrellas() {
        assertEquals(3, calcularEstrellas(aciertos = 4, total = 4))
        assertEquals(3, calcularEstrellas(aciertos = 1, total = 1))
    }

    @Test
    fun mitadOMasDanDosEstrellas() {
        assertEquals(2, calcularEstrellas(aciertos = 3, total = 4))
        assertEquals(2, calcularEstrellas(aciertos = 2, total = 4))
    }

    @Test
    fun menosDeLaMitadDaUnaEstrella() {
        assertEquals(1, calcularEstrellas(aciertos = 1, total = 4))
        assertEquals(1, calcularEstrellas(aciertos = 0, total = 4))
    }

    // La mitad se calcula con división entera: con 3 preguntas, 1 acierto ya cuenta como "mitad"
    @Test
    fun conTotalImparLaMitadSeRedondeaHaciaAbajo() {
        assertEquals(2, calcularEstrellas(aciertos = 1, total = 3))
        assertEquals(1, calcularEstrellas(aciertos = 0, total = 3))
    }
}
