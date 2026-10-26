package com.example.turkids

import com.example.turkids.model.UsuarioUiState
import com.example.turkids.model.ValidadorUsuario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorUsuarioTest {

    private val validador = ValidadorUsuario()

    @Test
    fun nombreVacioOSoloEspaciosEsInvalido() {
        assertEquals(ValidadorUsuario.MENSAJE_OBLIGATORIO, validador.validarNombre(""))
        assertEquals(ValidadorUsuario.MENSAJE_OBLIGATORIO, validador.validarNombre("   "))
    }

    @Test
    fun nombreConTextoEsValido() {
        assertNull(validador.validarNombre("Ayşe"))
    }

    @Test
    fun direccionVaciaEsInvalidaYConTextoEsValida() {
        assertEquals(ValidadorUsuario.MENSAJE_OBLIGATORIO, validador.validarDireccion(""))
        assertEquals(ValidadorUsuario.MENSAJE_OBLIGATORIO, validador.validarDireccion("  "))
        assertNull(validador.validarDireccion("Calle Falsa 123"))
    }

    @Test
    fun correosConFormatoCorrectoSonValidos() {
        listOf("ana@correo.cl", "ana.perez+tag@dominio.com", "a_b-c@sub.dominio.org").forEach {
            assertNull("Debería ser válido: $it", validador.validarCorreo(it))
        }
    }

    @Test
    fun correosConFormatoIncorrectoSonInvalidos() {
        listOf("", "ana", "ana@", "@correo.cl", "ana@correo", "ana correo@x.cl", "ana@.cl", "ana@correo..cl")
            .forEach {
                assertEquals(
                    "Debería ser inválido: '$it'",
                    ValidadorUsuario.MENSAJE_CORREO,
                    validador.validarCorreo(it)
                )
            }
    }

    @Test
    fun claveRequiereAlMenosSeisCaracteres() {
        assertEquals(ValidadorUsuario.MENSAJE_CLAVE, validador.validarClave(""))
        assertEquals(ValidadorUsuario.MENSAJE_CLAVE, validador.validarClave("12345"))
        assertNull(validador.validarClave("123456"))
        assertNull(validador.validarClave("una clave larga"))
    }

    @Test
    fun terminosDebenEstarAceptados() {
        assertEquals(ValidadorUsuario.MENSAJE_TERMINOS, validador.validarTerminos(false))
        assertNull(validador.validarTerminos(true))
    }

    @Test
    fun formularioVacioTieneErroresEnTodosLosCampos() {
        val errores = validador.validar(UsuarioUiState())
        assertNotNull(errores.nombre)
        assertNotNull(errores.correo)
        assertNotNull(errores.clave)
        assertNotNull(errores.direccion)
        assertNotNull(errores.terminos)
        assertFalse(errores.sinErrores)
    }

    @Test
    fun formularioCompletoNoTieneErrores() {
        val estado = UsuarioUiState(
            nombre = "Ana",
            correo = "ana@correo.cl",
            clave = "secreta1",
            direccion = "Calle 1",
            aceptaTerminos = true
        )
        assertTrue(validador.validar(estado).sinErrores)
    }
}
