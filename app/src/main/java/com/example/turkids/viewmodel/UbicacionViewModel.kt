package com.example.turkids.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.turkids.model.ProblemaUbicacion
import com.example.turkids.model.UbicacionEstado
import com.example.turkids.repository.UbicacionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Solo mantiene el estado; el acceso al dispositivo está en UbicacionRepository
class UbicacionViewModel(private val repositorio: UbicacionRepository) : ViewModel() {

    private val _estado = MutableStateFlow<UbicacionEstado>(UbicacionEstado.Verificando)
    val estado: StateFlow<UbicacionEstado> = _estado

    private val _problema = MutableStateFlow(ProblemaUbicacion.NINGUNO)
    val problema: StateFlow<ProblemaUbicacion> = _problema

    var solicitudIniciada = false
        private set

    private var permisoRespondido = false
    private var puedeVolverAPedir = false
    private var trabajo: Job? = null

    fun iniciarSolicitud() {
        solicitudIniciada = true
    }

    // puedeVolverAPedir: el sistema aún permite mostrar el diálogo (shouldShowRequestPermissionRationale)
    fun registrarResultado(puedeVolverAPedir: Boolean = false) {
        solicitudIniciada = true
        permisoRespondido = true
        this.puedeVolverAPedir = puedeVolverAPedir
        actualizar()
    }

    fun permisoConcedido(): Boolean = repositorio.permisoConcedido()

    fun actualizar() {
        trabajo?.cancel()
        trabajo = viewModelScope.launch { _estado.value = consultarEstado() }
    }

    private suspend fun consultarEstado(): UbicacionEstado {
        if (!repositorio.permisoConcedido()) {
            _problema.value = when {
                !permisoRespondido -> ProblemaUbicacion.NINGUNO
                puedeVolverAPedir -> ProblemaUbicacion.PERMISO_DENEGADO
                else -> ProblemaUbicacion.PERMISO_BLOQUEADO
            }
            return UbicacionEstado.NoEnUso
        }
        if (!repositorio.ubicacionActivada()) {
            _problema.value = ProblemaUbicacion.UBICACION_DESACTIVADA
            return UbicacionEstado.NoEnUso
        }

        _problema.value = ProblemaUbicacion.NINGUNO
        if (_estado.value == UbicacionEstado.NoEnUso) _estado.value = UbicacionEstado.Verificando

        val lugar = repositorio.obtenerNombreDelLugar() ?: return UbicacionEstado.NoEnUso
        return UbicacionEstado.EnUso(lugar)
    }
}
