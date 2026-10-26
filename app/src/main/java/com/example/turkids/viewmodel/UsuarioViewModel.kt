package com.example.turkids.viewmodel

import androidx.lifecycle.ViewModel
import com.example.turkids.model.CampoUsuario
import com.example.turkids.model.UsuarioUiState
import com.example.turkids.model.ValidadorUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

// Solo orquesta el estado: las reglas viven en ValidadorUsuario
class UsuarioViewModel(private val validador: ValidadorUsuario) : ViewModel() {

    private val _estado = MutableStateFlow(UsuarioUiState())
    val estado: StateFlow<UsuarioUiState> = _estado

    fun onNombreChange(valor: String) = cambiar { it.copy(nombre = valor) }

    fun onCorreoChange(valor: String) = cambiar { it.copy(correo = valor) }

    fun onClaveChange(valor: String) = cambiar { it.copy(clave = valor) }

    fun onDireccionChange(valor: String) = cambiar { it.copy(direccion = valor) }

    // Marcar los términos es una interacción completa: su error ya puede mostrarse
    fun onAceptarTerminosChange(valor: Boolean) = cambiar {
        it.copy(aceptaTerminos = valor, camposTocados = it.camposTocados + CampoUsuario.TERMINOS)
    }

    // La interfaz avisa cuando la persona sale de un campo; desde ahí se muestra su error
    fun onCampoVisitado(campo: CampoUsuario) = cambiar {
        it.copy(camposTocados = it.camposTocados + campo)
    }

    fun validarFormulario(): Boolean {
        _estado.update { actual ->
            conErrores(actual.copy(camposTocados = CampoUsuario.entries.toSet()))
        }
        return _estado.value.errores.sinErrores
    }

    fun limpiar() {
        _estado.value = UsuarioUiState()
    }

    private fun cambiar(transformar: (UsuarioUiState) -> UsuarioUiState) {
        _estado.update { conErrores(transformar(it)) }
    }

    private fun conErrores(estado: UsuarioUiState): UsuarioUiState =
        estado.copy(errores = validador.validar(estado).soloDe(estado.camposTocados))
}
