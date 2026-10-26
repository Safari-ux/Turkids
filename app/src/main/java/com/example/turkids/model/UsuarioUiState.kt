package com.example.turkids.model

enum class CampoUsuario { NOMBRE, CORREO, CLAVE, DIRECCION, TERMINOS }

data class UsuarioErrores(
    val nombre: String? = null,
    val correo: String? = null,
    val clave: String? = null,
    val direccion: String? = null,
    val terminos: String? = null
) {
    val sinErrores: Boolean
        get() = listOfNotNull(nombre, correo, clave, direccion, terminos).isEmpty()

    // Deja solo los errores de los campos indicados (los que ya se pueden mostrar)
    fun soloDe(campos: Set<CampoUsuario>): UsuarioErrores = UsuarioErrores(
        nombre = nombre.takeIf { CampoUsuario.NOMBRE in campos },
        correo = correo.takeIf { CampoUsuario.CORREO in campos },
        clave = clave.takeIf { CampoUsuario.CLAVE in campos },
        direccion = direccion.takeIf { CampoUsuario.DIRECCION in campos },
        terminos = terminos.takeIf { CampoUsuario.TERMINOS in campos }
    )
}

data class UsuarioUiState(
    val nombre: String = "",
    val correo: String = "",
    val clave: String = "",
    val direccion: String = "",
    val aceptaTerminos: Boolean = false,
    // Campos con los que la persona ya interactuó: solo de esos se muestra el error
    val camposTocados: Set<CampoUsuario> = emptySet(),
    val errores: UsuarioErrores = UsuarioErrores()
)
