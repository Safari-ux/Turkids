@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.turkids.ui.screens

import com.example.turkids.viewmodel.MainViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.turkids.model.calcularEstrellas
import com.example.turkids.navigation.Screen
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun ResumenScreen(aciertos: Int, total: Int, mainViewModel: MainViewModel) {
    val estrellas = calcularEstrellas(aciertos, total)

    var contenidoVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { contenidoVisible = true }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Resumen de la lección") }) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Confeti(modifier = Modifier.fillMaxSize())

            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AnimatedVisibility(
                    visible = contenidoVisible,
                    enter = scaleIn(tween(500)) + fadeIn(tween(500))
                ) {
                    Text(text = "⭐".repeat(estrellas).ifEmpty { "🔄" }, fontSize = 48.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(
                    visible = contenidoVisible,
                    enter = fadeIn(tween(600, delayMillis = 150))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "¡Lección completada!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Respuestas correctas: $aciertos de $total",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                AnimatedVisibility(
                    visible = contenidoVisible,
                    enter = fadeIn(tween(600, delayMillis = 300))
                ) {
                    Button(
                        onClick = {
                            mainViewModel.navigateTo(
                                screen = Screen.Home,
                                popUpToRoute = Screen.Home,
                                inclusive = true
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Volver al inicio", fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun Confeti(modifier: Modifier = Modifier) {
    val colores = remember {
        listOf(
            Color(0xFFFFC800), Color(0xFF58CC02), Color(0xFF1CB0F6),
            Color(0xFFFF5A5F), Color(0xFFCE82FF)
        )
    }
    val particulas = remember {
        List(40) {
            ParticulaConfeti(
                x = Random.nextFloat(),
                retraso = Random.nextFloat() * 0.3f,
                amplitud = Random.nextFloat() * 40f + 10f,
                frecuencia = Random.nextFloat() * 3f + 1f,
                velocidadRotacion = Random.nextFloat() * 6f + 2f,
                color = colores[Random.nextInt(colores.size)]
            )
        }
    }
    val progreso = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progreso.animateTo(1f, animationSpec = tween(2200, easing = LinearEasing))
    }

    Canvas(modifier = modifier) {
        particulas.forEach { p ->
            val t = ((progreso.value - p.retraso) / (1f - p.retraso)).coerceIn(0f, 1f)
            if (t <= 0f) return@forEach
            val y = t * size.height
            val x = p.x * size.width + sin(t * PI.toFloat() * p.frecuencia) * p.amplitud
            val alpha = (1f - t).coerceIn(0f, 1f)
            rotate(degrees = t * 360f * p.velocidadRotacion, pivot = Offset(x, y)) {
                drawRect(
                    color = p.color.copy(alpha = alpha),
                    topLeft = Offset(x - 4f, y - 4f),
                    size = Size(8f, 8f)
                )
            }
        }
    }
}

private data class ParticulaConfeti(
    val x: Float,
    val retraso: Float,
    val amplitud: Float,
    val frecuencia: Float,
    val velocidadRotacion: Float,
    val color: Color
)