package com.example.turkids.ui.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast

// Ajustes de esta app (para cambiar el permiso de ubicación)
fun Context.abrirAjustesDeLaApp() {
    abrir(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
    )
}

// Ajustes de ubicación del dispositivo (para encender el GPS/ubicación)
fun Context.abrirAjustesDeUbicacion() {
    abrir(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
}

private fun Context.abrir(intent: Intent) {
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, "No se pudo abrir los ajustes", Toast.LENGTH_SHORT).show()
    }
}
