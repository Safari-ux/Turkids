package com.example.turkids.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.turkids.model.ProblemaUbicacion
import com.example.turkids.model.UbicacionEstado
import com.example.turkids.model.texto
import com.example.turkids.ui.utils.abrirAjustesDeLaApp
import com.example.turkids.ui.utils.abrirAjustesDeUbicacion

// Muestra el estado de la ubicación y, si no se puede usar, explica por qué y ofrece la acción para arreglarlo
@Composable
fun IndicadorUbicacion(
    estado: UbicacionEstado,
    modifier: Modifier = Modifier,
    problema: ProblemaUbicacion = ProblemaUbicacion.NINGUNO,
    onReintentarPermiso: () -> Unit = {}
) {
    val contexto = LocalContext.current
    val enUso = estado is UbicacionEstado.EnUso
    val colorFondo = if (enUso) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val colorTexto = if (enUso) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colorFondo,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "\uD83D\uDCCD", fontSize = 16.sp)
                Text(text = estado.texto(), fontSize = 14.sp, color = colorTexto)
            }

            explicacion(problema)?.let {
                Text(text = it, fontSize = 13.sp, color = colorTexto)
            }

            when (problema) {
                ProblemaUbicacion.PERMISO_DENEGADO -> Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = onReintentarPermiso) { Text("Permitir") }
                    OutlinedButton(onClick = { contexto.abrirAjustesDeLaApp() }) { Text("Abrir ajustes") }
                }
                ProblemaUbicacion.PERMISO_BLOQUEADO ->
                    Button(onClick = { contexto.abrirAjustesDeLaApp() }) { Text("Abrir ajustes") }
                ProblemaUbicacion.UBICACION_DESACTIVADA ->
                    Button(onClick = { contexto.abrirAjustesDeUbicacion() }) { Text("Activar ubicación") }
                ProblemaUbicacion.NINGUNO -> Unit
            }
        }
    }
}

private fun explicacion(problema: ProblemaUbicacion): String? = when (problema) {
    ProblemaUbicacion.PERMISO_DENEGADO ->
        "Usamos tu ubicación solo para mostrar en qué región estás. Puedes permitirla cuando quieras."
    ProblemaUbicacion.PERMISO_BLOQUEADO ->
        "El permiso de ubicación está desactivado. Puedes activarlo en los ajustes de la app."
    ProblemaUbicacion.UBICACION_DESACTIVADA ->
        "La ubicación del dispositivo está apagada. Actívala para ver tu región."
    ProblemaUbicacion.NINGUNO -> null
}
