package com.example.turkids.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.turkids.model.ValidadorUsuario
import com.example.turkids.repository.ProgresoRepository
import com.example.turkids.repository.UbicacionRepository

// Fábrica sencilla: recibe las dependencias ya armadas (se crean en MainActivity) y las entrega a cada ViewModel
class TurKidsViewModelFactory(
    private val progresoRepository: ProgresoRepository,
    private val ubicacionRepository: UbicacionRepository,
    private val validadorUsuario: ValidadorUsuario
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(MainViewModel::class.java) -> MainViewModel()
        modelClass.isAssignableFrom(ProgresoViewModel::class.java) ->
            ProgresoViewModel(progresoRepository)
        modelClass.isAssignableFrom(LeccionViewModel::class.java) ->
            LeccionViewModel(progresoRepository)
        modelClass.isAssignableFrom(UbicacionViewModel::class.java) ->
            UbicacionViewModel(ubicacionRepository)
        modelClass.isAssignableFrom(UsuarioViewModel::class.java) ->
            UsuarioViewModel(validadorUsuario)
        else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    } as T
}
