package com.example.turkids.ui.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.turkids.viewmodel.UbicacionViewModel

// Reconsulta al volver a la app (por ejemplo desde Ajustes) y cuando se activa o desactiva la ubicación
@Composable
fun ObservarUbicacion(ubicacionViewModel: UbicacionViewModel) {
    val actividad = LocalActivity.current as ComponentActivity

    DisposableEffect(actividad) {
        var registrado = false
        val receptor = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                ubicacionViewModel.actualizar()
            }
        }

        val observador = LifecycleEventObserver { _, evento ->
            when (evento) {
                Lifecycle.Event.ON_START -> if (!registrado) {
                    ContextCompat.registerReceiver(
                        actividad,
                        receptor,
                        IntentFilter(LocationManager.MODE_CHANGED_ACTION),
                        ContextCompat.RECEIVER_NOT_EXPORTED
                    )
                    registrado = true
                }
                Lifecycle.Event.ON_RESUME -> ubicacionViewModel.actualizar()
                Lifecycle.Event.ON_STOP -> if (registrado) {
                    actividad.unregisterReceiver(receptor)
                    registrado = false
                }
                else -> Unit
            }
        }

        actividad.lifecycle.addObserver(observador)
        onDispose {
            actividad.lifecycle.removeObserver(observador)
            if (registrado) {
                actividad.unregisterReceiver(receptor)
                registrado = false
            }
        }
    }
}
