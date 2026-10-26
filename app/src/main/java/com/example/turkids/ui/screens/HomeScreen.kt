@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.turkids.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.turkids.R
import com.example.turkids.model.BancoDeContenido
import com.example.turkids.model.PerfilesPrueba
import com.example.turkids.model.ProblemaUbicacion
import com.example.turkids.model.ProgresoUiState
import com.example.turkids.model.UbicacionEstado
import com.example.turkids.model.Unidad
import com.example.turkids.navigation.Screen
import com.example.turkids.ui.utils.obtenerWindowSizeClass
import com.example.turkids.viewmodel.MainViewModel
import com.example.turkids.viewmodel.ProgresoViewModel
import com.example.turkids.viewmodel.UbicacionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreenAdaptativo(
    mainViewModel: MainViewModel,
    progresoViewModel: ProgresoViewModel,
    ubicacionViewModel: UbicacionViewModel,
    onSolicitarPermisoUbicacion: () -> Unit
) {
    val progreso by progresoViewModel.estado.collectAsState()
    val ubicacion by ubicacionViewModel.estado.collectAsState()
    val problemaUbicacion by ubicacionViewModel.problema.collectAsState()
    val columnas = when (obtenerWindowSizeClass().widthSizeClass) {
        WindowWidthSizeClass.Medium -> 2
        WindowWidthSizeClass.Expanded -> 3
        else -> 1
    }

    HomeScreenBase(
        mainViewModel = mainViewModel,
        columnas = columnas,
        progreso = progreso,
        ubicacion = ubicacion,
        problemaUbicacion = problemaUbicacion,
        onSolicitarPermisoUbicacion = onSolicitarPermisoUbicacion,
        onPerfilClick = { progresoViewModel.seleccionarPerfil(it) },
        onReiniciar = { progresoViewModel.reiniciar() }
    )
}

@Composable
private fun HomeScreenBase(
    mainViewModel: MainViewModel,
    columnas: Int,
    progreso: ProgresoUiState,
    ubicacion: UbicacionEstado,
    problemaUbicacion: ProblemaUbicacion,
    onSolicitarPermisoUbicacion: () -> Unit,
    onPerfilClick: (String) -> Unit,
    onReiniciar: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "Menú",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider()
                BancoDeContenido.unidades.forEach { unidad ->
                    NavigationDrawerItem(
                        label = { Text("${unidad.emoji}  ${unidad.titulo}") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            mainViewModel.navigateTo(Screen.Leccion(unidad.id))
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
                NavigationDrawerItem(
                    label = { Text("ℹ️  Acerca de") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        mainViewModel.navigateTo(Screen.AcercaDe)
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("📝  Registro de prueba") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        mainViewModel.navigateTo(Screen.Registro)
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("🔄  Reiniciar progreso de este perfil") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onReiniciar()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Türkçe - Español", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    }
                )
            }
        ) { innerPadding ->
            if (progreso.cargando) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                return@Scaffold
            }

            Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
                var logoVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { logoVisible = true }
                AnimatedVisibility(
                    visible = logoVisible,
                    enter = fadeIn(tween(600)) + slideInVertically(
                        initialOffsetY = { -40 }, animationSpec = tween(600)
                    )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo de la app",
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                IndicadorUbicacion(
                    estado = ubicacion,
                    problema = problemaUbicacion,
                    onReintentarPermiso = onSolicitarPermisoUbicacion
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Perfil ficticio de prueba:", fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PerfilesPrueba.lista.forEach { perfil ->
                        FilterChip(
                            selected = perfil.id == progreso.perfilActivoId,
                            onClick = { onPerfilClick(perfil.id) },
                            label = { Text(perfil.nombre, fontSize = 18.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Practicando: ${progreso.idioma.etiqueta}",
                        fontSize = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = {
                            mainViewModel.navigateTo(
                                screen = Screen.Practica,
                                popUpToRoute = Screen.Home,
                                inclusive = true
                            )
                        }
                    ) {
                        Text("Cambiar", fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))

                Text(text = "Elige una unidad para practicar", fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(columnas),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(BancoDeContenido.unidades) { index, unidad ->
                        TarjetaUnidad(
                            unidad = unidad,
                            index = index,
                            estrellas = progreso.estrellas[unidad.id] ?: 0,
                            onClick = { mainViewModel.navigateTo(Screen.Leccion(unidad.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaUnidad(unidad: Unidad, index: Int, estrellas: Int, onClick: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(unidad.id) {
        delay(150L + index * 120L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + slideInVertically(initialOffsetY = { 60 }, animationSpec = tween(400))
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.94f else 1f,
            animationSpec = tween(120),
            label = "escalaTarjeta"
        )

        Surface(
            onClick = onClick,
            interactionSource = interactionSource,
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth().height(96.dp).scale(scale)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = unidad.emoji, fontSize = 40.sp)
                Text(
                    text = unidad.titulo,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "⭐".repeat(estrellas) + "☆".repeat(3 - estrellas),
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Preview(name = "Compact", widthDp = 360, heightDp = 800)
@Composable
private fun PreviewCompacta() {
    HomeScreenBase(
        MainViewModel(), 1, ProgresoUiState(), UbicacionEstado.NoEnUso,
        ProblemaUbicacion.PERMISO_BLOQUEADO, {}, {}, {}
    )
}

@Preview(name = "Medium", widthDp = 700, heightDp = 900)
@Composable
private fun PreviewMediana() {
    HomeScreenBase(
        MainViewModel(), 2, ProgresoUiState(), UbicacionEstado.EnUso("Región Metropolitana, Chile"),
        ProblemaUbicacion.NINGUNO, {}, {}, {}
    )
}

@Preview(name = "Expanded", widthDp = 1200, heightDp = 900)
@Composable
private fun PreviewExpandida() {
    HomeScreenBase(
        MainViewModel(), 3, ProgresoUiState(), UbicacionEstado.Verificando,
        ProblemaUbicacion.NINGUNO, {}, {}, {}
    )
}
