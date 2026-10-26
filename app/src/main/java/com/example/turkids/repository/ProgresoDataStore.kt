package com.example.turkids.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private const val NOMBRE_ARCHIVO = "progreso_turkids"

// Una sola instancia por proceso. Usa el mismo nombre que el antiguo SharedPreferences:
// la migración copia una única vez los datos existentes y luego borra el archivo viejo.
val Context.progresoDataStore: DataStore<Preferences> by preferencesDataStore(
    name = NOMBRE_ARCHIVO,
    produceMigrations = { contexto ->
        listOf(SharedPreferencesMigration(contexto, NOMBRE_ARCHIVO))
    }
)
