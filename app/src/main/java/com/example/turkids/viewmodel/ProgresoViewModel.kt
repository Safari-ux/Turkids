package com.example.turkids.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.turkids.model.BancoDeContenido
import com.example.turkids.model.IdiomaPractica
import com.example.turkids.model.ProgresoUiState
import com.example.turkids.model.calcularEstrellas
import com.example.turkids.repository.ProgresoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProgresoViewModel(private val repositorio: ProgresoRepository) : ViewModel() {

    private val idsUnidades = BancoDeContenido.unidades.map { it.id }

    // El estado sale de DataStore: cada escritura se refleja sola. Empieza en "cargando".
    @OptIn(ExperimentalCoroutinesApi::class)
    val estado: StateFlow<ProgresoUiState> = repositorio.perfilActivo
        .flatMapLatest { perfilId ->
            combine(
                repositorio.idiomaPractica,
                repositorio.estrellas(perfilId, idsUnidades)
            ) { idioma, estrellas ->
                ProgresoUiState(
                    perfilActivoId = perfilId,
                    idioma = idioma,
                    estrellas = estrellas,
                    cargando = false
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProgresoUiState(cargando = true)
        )

    fun seleccionarPerfil(perfilId: String) {
        viewModelScope.launch { repositorio.guardarPerfilActivo(perfilId) }
    }

    // alTerminar se llama cuando el idioma ya quedó guardado (así la pantalla siguiente no ve el valor anterior)
    fun seleccionarIdioma(idioma: IdiomaPractica, alTerminar: () -> Unit = {}) {
        viewModelScope.launch {
            repositorio.guardarIdiomaPractica(idioma)
            alTerminar()
        }
    }

    fun registrarResultado(unidadId: String, aciertos: Int, total: Int) {
        val perfilId = estado.value.perfilActivoId
        viewModelScope.launch {
            repositorio.guardarEstrellas(perfilId, unidadId, calcularEstrellas(aciertos, total))
        }
    }

    fun reiniciar() {
        val perfilId = estado.value.perfilActivoId
        viewModelScope.launch { repositorio.reiniciar(perfilId) }
    }
}
