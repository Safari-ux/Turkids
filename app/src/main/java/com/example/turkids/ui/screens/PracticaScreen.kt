@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.turkids.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.turkids.R
import com.example.turkids.model.IdiomaPractica
import com.example.turkids.navigation.Screen
import com.example.turkids.viewmodel.MainViewModel
import com.example.turkids.viewmodel.ProgresoViewModel

@Composable
fun PracticaScreen(
    mainViewModel: MainViewModel,
    progresoViewModel: ProgresoViewModel
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Türkçe - Español", fontWeight = FontWeight.Bold) })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo de la app",
                modifier = Modifier.height(140.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "¿Qué deseas practicar?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            IdiomaPractica.entries.forEach { idioma ->
                OpcionIdioma(
                    idioma = idioma,
                    onClick = {
                        // Se navega cuando el idioma ya quedó guardado, para que Home no muestre el anterior
                        progresoViewModel.seleccionarIdioma(idioma) {
                            mainViewModel.navigateTo(
                                screen = Screen.Home,
                                popUpToRoute = Screen.Practica,
                                inclusive = true
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun OpcionIdioma(idioma: IdiomaPractica, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 4.dp,
        modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = idioma.bandera, fontSize = 40.sp)
            Column {
                Text(
                    text = idioma.etiqueta,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = idioma.descripcion,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
