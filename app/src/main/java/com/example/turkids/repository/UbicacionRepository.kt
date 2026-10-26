package com.example.turkids.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume

val permisosUbicacion = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

// Único punto de acceso a LocationManager y Geocoder
class UbicacionRepository(contextoApp: Context) {

    private val contexto: Context = contextoApp.applicationContext
    private val administrador =
        contexto.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun permisoConcedido(): Boolean = permisosUbicacion.any { tienePermiso(it) }

    fun ubicacionActivada(): Boolean = LocationManagerCompat.isLocationEnabled(administrador)

    // Nombre del lugar actual (región y país, o coordenadas si no hay geocodificador); null si no se obtuvo ubicación
    suspend fun obtenerNombreDelLugar(): String? {
        val ubicacion = obtenerUbicacion() ?: return null
        return nombreDelLugar(ubicacion)
    }

    private fun tienePermiso(permiso: String): Boolean =
        ContextCompat.checkSelfPermission(contexto, permiso) == PackageManager.PERMISSION_GRANTED

    // La red alcanza para país y región; el GPS solo se usa si hay permiso de ubicación precisa
    private fun proveedoresDisponibles(): List<String> =
        listOfNotNull(
            LocationManager.NETWORK_PROVIDER,
            LocationManager.GPS_PROVIDER.takeIf { tienePermiso(Manifest.permission.ACCESS_FINE_LOCATION) }
        ).filter { administrador.isProviderEnabled(it) }

    private suspend fun obtenerUbicacion(): Location? {
        for (proveedor in proveedoresDisponibles()) {
            val ubicacion = withTimeoutOrNull(TIEMPO_ESPERA_MS) { ubicacionActual(proveedor) }
                ?: ultimaConocida(proveedor)
            if (ubicacion != null) return ubicacion
        }
        return null
    }

    @SuppressLint("MissingPermission")
    private suspend fun ubicacionActual(proveedor: String): Location? =
        suspendCancellableCoroutine { continuacion ->
            val senal = CancellationSignal()
            continuacion.invokeOnCancellation { senal.cancel() }
            try {
                LocationManagerCompat.getCurrentLocation(
                    administrador,
                    proveedor,
                    senal,
                    ContextCompat.getMainExecutor(contexto)
                ) { ubicacion -> continuacion.resume(ubicacion) }
            } catch (e: SecurityException) {
                continuacion.resume(null)
            }
        }

    @SuppressLint("MissingPermission")
    private fun ultimaConocida(proveedor: String): Location? =
        try {
            administrador.getLastKnownLocation(proveedor)
        } catch (e: SecurityException) {
            null
        }

    // Si no hay geocodificador disponible se muestran las coordenadas
    private suspend fun nombreDelLugar(ubicacion: Location): String {
        val direccion = buscarDireccion(ubicacion)
        val lugar = listOfNotNull(direccion?.adminArea, direccion?.countryName)
            .filter { it.isNotBlank() }
            .joinToString(", ")
        return lugar.ifEmpty {
            String.format(Locale.US, "%.2f, %.2f", ubicacion.latitude, ubicacion.longitude)
        }
    }

    private suspend fun buscarDireccion(ubicacion: Location): Address? {
        if (!Geocoder.isPresent()) return null
        val geocoder = Geocoder(contexto, Locale.getDefault())
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuacion ->
                    geocoder.getFromLocation(
                        ubicacion.latitude,
                        ubicacion.longitude,
                        1,
                        object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<Address>) {
                                continuacion.resume(addresses.firstOrNull())
                            }

                            override fun onError(errorMessage: String?) {
                                continuacion.resume(null)
                            }
                        }
                    )
                }
            } else {
                withContext(Dispatchers.IO) {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(ubicacion.latitude, ubicacion.longitude, 1)?.firstOrNull()
                }
            }
        } catch (e: IOException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private companion object {
        const val TIEMPO_ESPERA_MS = 6_000L
    }
}
