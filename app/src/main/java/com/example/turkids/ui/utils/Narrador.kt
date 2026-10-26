package com.example.turkids.ui.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.turkids.model.Pregunta
import java.util.Locale

// Envuelve TextToSpeech: la pantalla solo pide "hablar" y no conoce el motor de voz
class Narrador(private val contexto: Context) {

    private var motor: TextToSpeech? = null
    private var listo = false

    fun iniciar() {
        motor = TextToSpeech(contexto) { estado -> listo = estado == TextToSpeech.SUCCESS }
    }

    fun detener() {
        motor?.stop()
    }

    fun liberar() {
        motor?.stop()
        motor?.shutdown()
        motor = null
        listo = false
    }

    // La palabra que se muestra en pantalla es la que se pronuncia
    fun hablar(pregunta: Pregunta, avisar: Boolean) {
        if (pregunta.mostrarTurco) {
            hablar(pregunta.palabra.tr, IDIOMA_TURCO, avisar)
        } else {
            hablar(pregunta.palabra.es, IDIOMA_ESPANOL, avisar)
        }
    }

    // avisar = true muestra un mensaje si el motor o la voz no están disponibles
    fun hablar(texto: String, idioma: Locale, avisar: Boolean) {
        val motorActual = motor
        if (motorActual == null || !listo) {
            if (avisar) {
                Toast.makeText(contexto, "El motor de voz aún no está listo", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val resultado = motorActual.setLanguage(idioma)
        if (resultado == TextToSpeech.LANG_MISSING_DATA || resultado == TextToSpeech.LANG_NOT_SUPPORTED) {
            if (avisar) {
                Toast.makeText(
                    contexto,
                    "Falta instalar la voz: ${idioma.displayLanguage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            motorActual.speak(texto, TextToSpeech.QUEUE_FLUSH, null, ID_ENUNCIADO)
        }
    }

    private companion object {
        val IDIOMA_TURCO: Locale = Locale.forLanguageTag("tr-TR")
        val IDIOMA_ESPANOL: Locale = Locale.forLanguageTag("es-US")
        const val ID_ENUNCIADO = "turkids"
    }
}

// Crea el narrador al entrar a la pantalla, lo libera al salir y calla el audio cuando la app deja de verse
@Composable
fun rememberNarrador(): Narrador {
    val contexto = LocalContext.current
    val narrador = remember { Narrador(contexto) }

    DisposableEffect(narrador) {
        narrador.iniciar()
        onDispose { narrador.liberar() }
    }

    val ciclo = (LocalActivity.current as ComponentActivity).lifecycle
    DisposableEffect(ciclo, narrador) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_STOP) narrador.detener()
        }
        ciclo.addObserver(observador)
        onDispose { ciclo.removeObserver(observador) }
    }

    return narrador
}
