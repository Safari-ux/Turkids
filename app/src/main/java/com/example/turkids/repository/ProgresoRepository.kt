package com.example.turkids.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.turkids.model.IdiomaPractica
import com.example.turkids.model.LeccionGuardada
import com.example.turkids.model.PerfilesPrueba
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException

// El avance se guarda solo en el dispositivo: perfil ficticio, idioma, estrellas por unidad y lección en curso.
// Las lecturas son Flow y las escrituras son funciones suspend.
class ProgresoRepository(private val dataStore: DataStore<Preferences>) {

    private val datos: Flow<Preferences> = dataStore.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }

    val perfilActivo: Flow<String> = datos
        .map { it[CLAVE_PERFIL] ?: PerfilesPrueba.lista.first().id }
        .distinctUntilChanged()

    val idiomaPractica: Flow<IdiomaPractica> = datos
        .map { idiomaDesde(it[CLAVE_IDIOMA]) ?: IdiomaPractica.TURCO }
        .distinctUntilChanged()

    fun estrellas(perfilId: String, unidadIds: List<String>): Flow<Map<String, Int>> =
        datos
            .map { prefs -> unidadIds.associateWith { prefs[claveEstrellas(perfilId, it)] ?: 0 } }
            .distinctUntilChanged()

    suspend fun guardarPerfilActivo(perfilId: String) {
        escribir { it[CLAVE_PERFIL] = perfilId }
    }

    suspend fun guardarIdiomaPractica(idioma: IdiomaPractica) {
        escribir { it[CLAVE_IDIOMA] = idioma.name }
    }

    // Se conserva el mejor resultado de cada unidad
    suspend fun guardarEstrellas(perfilId: String, unidadId: String, estrellas: Int) {
        val clave = claveEstrellas(perfilId, unidadId)
        escribir { prefs -> prefs[clave] = maxOf(prefs[clave] ?: 0, estrellas) }
    }

    suspend fun leccionGuardada(perfilId: String): LeccionGuardada? {
        val prefs = datos.first()
        val prefijo = prefijoLeccion(perfilId)
        val unidadId = prefs[stringPreferencesKey("${prefijo}unidad")] ?: return null
        val idioma = idiomaDesde(prefs[stringPreferencesKey("${prefijo}idioma")]) ?: return null
        return LeccionGuardada(
            unidadId = unidadId,
            idioma = idioma,
            semilla = prefs[longPreferencesKey("${prefijo}semilla")] ?: 0L,
            indice = prefs[intPreferencesKey("${prefijo}indice")] ?: 0,
            aciertos = prefs[intPreferencesKey("${prefijo}aciertos")] ?: 0
        )
    }

    suspend fun guardarLeccion(perfilId: String, leccion: LeccionGuardada) {
        val prefijo = prefijoLeccion(perfilId)
        escribir { prefs ->
            prefs[stringPreferencesKey("${prefijo}unidad")] = leccion.unidadId
            prefs[stringPreferencesKey("${prefijo}idioma")] = leccion.idioma.name
            prefs[longPreferencesKey("${prefijo}semilla")] = leccion.semilla
            prefs[intPreferencesKey("${prefijo}indice")] = leccion.indice
            prefs[intPreferencesKey("${prefijo}aciertos")] = leccion.aciertos
        }
    }

    suspend fun borrarLeccion(perfilId: String) {
        val prefijo = prefijoLeccion(perfilId)
        escribir { prefs -> quitarConPrefijos(prefs, listOf(prefijo)) }
    }

    suspend fun reiniciar(perfilId: String) {
        val prefijos = listOf("estrellas_${perfilId}_", prefijoLeccion(perfilId))
        escribir { prefs -> quitarConPrefijos(prefs, prefijos) }
    }

    // La escritura termina aunque la pantalla que la pidió se cierre justo después
    private suspend fun escribir(cambio: (MutablePreferences) -> Unit) {
        withContext(NonCancellable) {
            dataStore.edit { prefs -> cambio(prefs) }
        }
    }

    private fun quitarConPrefijos(prefs: MutablePreferences, prefijos: List<String>) {
        prefs.asMap().keys
            .filter { clave -> prefijos.any { clave.name.startsWith(it) } }
            .forEach { clave ->
                @Suppress("UNCHECKED_CAST")
                prefs.remove(clave as Preferences.Key<Any>)
            }
    }

    private fun idiomaDesde(nombre: String?): IdiomaPractica? =
        IdiomaPractica.entries.firstOrNull { it.name == nombre }

    private fun claveEstrellas(perfilId: String, unidadId: String) =
        intPreferencesKey("estrellas_${perfilId}_$unidadId")

    private fun prefijoLeccion(perfilId: String) = "leccion_${perfilId}_"

    private companion object {
        val CLAVE_PERFIL = stringPreferencesKey("perfil_activo")
        val CLAVE_IDIOMA = stringPreferencesKey("idioma_practica")
    }
}
