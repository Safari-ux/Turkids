package com.example.turkids

import com.example.turkids.model.UbicacionEstado
import com.example.turkids.model.texto
import org.junit.Assert.assertEquals
import org.junit.Test

class UbicacionEstadoTest {
    @Test
    fun sinPermisoOUbicacionDesactivadaMuestraElTextoExacto() {
        assertEquals("Tu ubicación no está en uso", UbicacionEstado.NoEnUso.texto())
    }

    @Test
    fun conUbicacionMuestraElLugar() {
        assertEquals(
            "Ubicación actual: Santiago, Chile",
            UbicacionEstado.EnUso("Santiago, Chile").texto()
        )
    }
}
