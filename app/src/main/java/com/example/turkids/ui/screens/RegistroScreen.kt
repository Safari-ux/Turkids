@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.turkids.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.turkids.model.CampoUsuario
import com.example.turkids.navigation.Screen
import com.example.turkids.viewmodel.MainViewModel
import com.example.turkids.viewmodel.UsuarioViewModel

@Composable
fun RegistroScreen(
    mainViewModel: MainViewModel,
    usuarioViewModel: UsuarioViewModel
) {
    val estado by usuarioViewModel.estado.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de prueba") },
                navigationIcon = {
                    IconButton(onClick = { mainViewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Usa solo datos ficticios",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            CampoTexto(
                valor = estado.nombre,
                etiqueta = "Nombre",
                icono = Icons.Default.Person,
                error = estado.errores.nombre,
                onCambio = usuarioViewModel::onNombreChange,
                onVisitado = { usuarioViewModel.onCampoVisitado(CampoUsuario.NOMBRE) }
            )
            CampoTexto(
                valor = estado.correo,
                etiqueta = "Correo electrónico",
                icono = Icons.Default.Email,
                error = estado.errores.correo,
                onCambio = usuarioViewModel::onCorreoChange,
                onVisitado = { usuarioViewModel.onCampoVisitado(CampoUsuario.CORREO) },
                teclado = KeyboardType.Email
            )
            CampoTexto(
                valor = estado.clave,
                etiqueta = "Contraseña",
                icono = Icons.Default.Lock,
                error = estado.errores.clave,
                onCambio = usuarioViewModel::onClaveChange,
                onVisitado = { usuarioViewModel.onCampoVisitado(CampoUsuario.CLAVE) },
                esClave = true
            )
            CampoTexto(
                valor = estado.direccion,
                etiqueta = "Dirección",
                icono = Icons.Default.LocationOn,
                error = estado.errores.direccion,
                onCambio = usuarioViewModel::onDireccionChange,
                onVisitado = { usuarioViewModel.onCampoVisitado(CampoUsuario.DIRECCION) }
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = estado.aceptaTerminos,
                    onCheckedChange = usuarioViewModel::onAceptarTerminosChange
                )
                Text("Acepto los términos y condiciones")
            }
            estado.errores.terminos?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    if (usuarioViewModel.validarFormulario()) {
                        mainViewModel.navigateTo(Screen.ResumenRegistro)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Registrar", fontSize = 18.sp)
            }
        }
    }
}

// onVisitado se llama al salir del campo: desde ahí se muestra su error
@Composable
private fun CampoTexto(
    valor: String,
    etiqueta: String,
    icono: ImageVector,
    error: String?,
    onCambio: (String) -> Unit,
    onVisitado: () -> Unit,
    teclado: KeyboardType = KeyboardType.Text,
    esClave: Boolean = false
) {
    val hayError = error != null
    var tuvoFoco by remember { mutableStateOf(false) }
    var claveVisible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        leadingIcon = {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = if (hayError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = if (esClave || hayError) {
            {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (hayError) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Campo con error",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    if (esClave) {
                        IconButton(onClick = { claveVisible = !claveVisible }) {
                            Icon(
                                imageVector = if (claveVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (claveVisible) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    }
                }
            }
        } else {
            null
        },
        isError = hayError,
        supportingText = {
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        },
        visualTransformation = if (esClave && !claveVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = KeyboardOptions(keyboardType = if (esClave) KeyboardType.Password else teclado),
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { foco ->
                if (foco.isFocused) {
                    tuvoFoco = true
                } else if (tuvoFoco) {
                    tuvoFoco = false
                    onVisitado()
                }
            }
    )
}
