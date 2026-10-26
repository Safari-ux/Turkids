# TurKids (Türkçe - Español)

Documento con la información actual del proyecto: contexto, arquitectura, pantallas, lógica, configuración, estado de Git y pendientes.

> **Estado de verificación:** las pruebas unitarias de la lógica en Kotlin puro (validador, estrellas y generación de preguntas) se ejecutaron y pasan. La refactorización de persistencia (DataStore), ubicación y pantallas **no se compiló con Gradle ni se probó en un dispositivo** en el entorno donde se armó, por lo que falta compilar y probar en Android Studio (ver sección 14).

---

## 1. Resumen

App móvil nativa en Kotlin (Jetpack Compose) para el aprendizaje inicial de español dirigido a niños y niñas de habla turca menores de 10 años. Funciona como un juego tipo Duolingo: se muestra una palabra con su emoji y el usuario elige entre 3 alternativas.

| Dato | Valor |
|---|---|
| Nombre en pantalla | Türkçe - Español |
| `rootProject.name` | TurKids |
| Package / applicationId | `com.example.turkids` |
| Asignatura | DSY1105 - Desarrollo de Aplicaciones Móviles |
| Trabajo | En parejas (Evaluación Parcial 2, encargo) |
| Contenido | Ficticio, de prueba, sin datos personales reales |

---

## 2. Contexto académico

**Evaluación Parcial 2**: interfaz funcional de una app móvil que resuelva una problemática real o simulada. Es una evaluación intermedia previa a la Evaluación Final Transversal (EFT).

- Asignación: semana 6. Entrega: semana 11 (antes de la primera sesión de Evaluación Parcial 3).
- Trabajo incompleto o fuera de plazo se califica con la nota mínima.
- Lo entregado sirve de base para el proyecto final.

### Requisitos y dónde se cubren

| Requisito | Dónde está en el proyecto |
|---|---|
| Diseño visual con Material 3 | Todas las pantallas (`Scaffold`, `TopAppBar`, `NavigationBar`, `ModalNavigationDrawer`, `FilterChip`, `Surface`, etc.) |
| Formularios validados | `RegistroScreen` (íconos, error por campo) + `UsuarioViewModel` + `ValidadorUsuario` (reglas desacopladas de la interfaz) |
| Navegación funcional | `NavHost` en `MainActivity`, eventos en `MainViewModel`, rutas en `Screen` |
| Gestión de estado | `StateFlow` + ViewModels (`LeccionViewModel`, `ProgresoViewModel`, `UsuarioViewModel`, `UbicacionViewModel`), con dependencias inyectadas por `TurKidsViewModelFactory` |
| Almacenamiento local | `ProgresoRepository` con Preferences DataStore |
| Uso de recursos nativos | Ubicación (`LocationManager` + `Geocoder`, con permisos en tiempo de ejecución) y síntesis de voz (`TextToSpeech`) |
| Animaciones | Transiciones de navegación, aparición de tarjetas y opciones, escala al presionar, confeti en el resumen |

---

## 3. Qué hace la app (flujo)

```
Practica ("¿Qué deseas practicar?")
   ├─ Turco   ┐
   └─ Español ┴──> Home
                     ├─ Unidad ──> Leccion ──> Resumen ──> Home
                     ├─ Menú lateral ──> Acerca de / Registro de prueba / Reiniciar progreso
                     └─ "Cambiar" ──> Practica
Registro ──> ResumenRegistro ──> Home
```

### Pantallas

| Pantalla | Archivo | Descripción |
|---|---|---|
| Practica | `PracticaScreen.kt` | Pantalla inicial. Dos tarjetas: **Turco** y **Español**. La elección define el idioma de todas las preguntas. |
| Home | `HomeScreen.kt` | Logo, indicador de ubicación (con acciones si no se puede usar), perfil ficticio (Prueba 1 / Prueba 2), idioma actual con botón **Cambiar**, y tarjetas de unidades con estrellas. Muestra un indicador de carga hasta que DataStore entrega el progreso. Se adapta al ancho de pantalla: 1, 2 o 3 columnas. Menú lateral con las unidades, Acerca de, Registro de prueba y Reiniciar progreso. |
| Leccion | `LeccionScreen.kt` | Barra de progreso, emoji de la palabra, botón **Escuchar**, pregunta, 3 opciones que aparecen de forma escalonada, mensajes de ánimo y botón **Continuar**. |
| Resumen | `ResumenScreen.kt` | Estrellas obtenidas, aciertos sobre el total y confeti animado. Botón **Volver al inicio**. |
| Registro | `RegistroScreen.kt` | Formulario de prueba con íconos por campo, error visible solo después de salir del campo (o al pulsar **Registrar**) y botón para mostrar/ocultar la contraseña. |
| ResumenRegistro | `ResumenRegistroScreen.kt` | Muestra los datos ingresados (la contraseña va oculta) y limpia el formulario al volver. |
| Acerca de | `AcercaDeScreen.kt` | Descripción del MVP, con `NavigationBar` (Inicio / Acerca de). |

---

## 4. Mecánica del juego

### Preguntas y alternativas
- `BancoDeContenido.generarPreguntas(unidad, idioma, semilla)` crea una pregunta por palabra de la unidad.
- Cada pregunta tiene **3 opciones**: la correcta y 2 distractores de la **misma unidad**, sin repetir.
- **Idioma de práctica:**
  - **Turco:** se muestra la palabra en turco y se responde en español.
  - **Español:** se muestra la palabra en español y se responde en turco.
  - Las preguntas **nunca se mezclan** entre idiomas.
- La **semilla** hace que la misma lección se pueda reproducir idéntica (necesario para retomar una lección guardada).

### Validación de respuesta (`LeccionViewModel`)
- Respuesta correcta: se marca, suma un acierto y aparece **Continuar**.
- Primer error: la opción se descarta (naranja) y se da una **segunda oportunidad** (criterio del especialista: el error no castiga).
- Segundo error: se muestra la respuesta correcta y se avanza sin sumar.

### Estrellas (`calcularEstrellas`)
| Resultado | Estrellas |
|---|---|
| Todas correctas | 3 |
| Mitad o más | 2 |
| Menos de la mitad | 1 |
| Sin preguntas | 0 |

Se conserva el **mejor resultado** de cada unidad por perfil.

### Audio
- `Narrador` (`ui/utils/Narrador.kt`) envuelve `TextToSpeech` y pronuncia la palabra que se ve en pantalla: turco (`tr-TR`) o español (`es-US`). `LeccionScreen` solo lo usa mediante `rememberNarrador()`.
- Se reproduce automáticamente al aparecer la pregunta (con un retraso de 800 ms) y con el botón **Escuchar**.
- Si falta la voz del idioma en el dispositivo, el botón muestra un aviso. El audio se detiene al salir de la app (`ON_STOP`).

---

## 5. Contenido (`BancoDeContenido`)

Contenido ficticio de prueba. Las unidades y su orden se ajustan según el especialista.

| Unidad (`id`) | Palabras (es - tr) |
|---|---|
| Saludos (`saludos`) | Hola - Merhaba · Gracias - Teşekkürler · Sí - Evet · No - Hayır |
| Animales (`animales`) | Gato - Kedi · Perro - Köpek · Pájaro - Kuş · Pez - Balık |
| Números (`numeros`) | Uno - Bir · Dos - İki · Tres - Üç · Cuatro - Dört |

Cada palabra lleva un emoji como recurso visual (no usa imágenes reales).

---

## 6. Persistencia local

**Preferences DataStore** (`androidx.datastore:datastore-preferences`), archivo `progreso_turkids`, gestionado por `ProgresoRepository`. La instancia única se declara en `repository/ProgresoDataStore.kt`.

| Qué se guarda | Clave |
|---|---|
| Perfil activo | `perfil_activo` |
| Idioma de práctica | `idioma_practica` |
| Estrellas por perfil y unidad | `estrellas_{perfilId}_{unidadId}` |
| Lección en curso (retoma) | `leccion_{perfilId}_unidad`, `_idioma`, `_semilla`, `_indice`, `_aciertos` |

- **API asíncrona:** las lecturas son `Flow` (`perfilActivo`, `idiomaPractica`, `estrellas(...)`) y las escrituras son funciones `suspend`. `ProgresoViewModel` combina los flujos y expone un `StateFlow<ProgresoUiState>` que empieza en `cargando = true`; Home y Lección muestran un indicador de carga hasta la primera lectura, así la interfaz no parpadea con valores por defecto.
- **Migración:** las claves y su nombre de archivo son los mismos que usaba SharedPreferences, por lo que `SharedPreferencesMigration` copia una sola vez los datos existentes al primer arranque.
- **Retoma de lección:** se guarda al pulsar **Continuar**, y se borra al terminar la lección. Si se vuelve a entrar a la misma unidad con el mismo idioma, continúa desde esa pregunta con sus aciertos. Hay un solo punto de retoma por perfil.
- **Estrellas:** solo se conserva el mejor resultado de cada unidad (la comparación se hace dentro de la misma transacción de escritura).
- **Reiniciar progreso** (menú lateral) borra las estrellas y la lección en curso del perfil activo.
- **Lo que no se guarda a propósito:** los datos del formulario de registro (son datos personales y la app está pensada para menores).
- No se usa Room.

---

## 7. Permisos y recursos nativos

### Ubicación
- Manifest: `ACCESS_COARSE_LOCATION` y `ACCESS_FINE_LOCATION`, más `uses-feature` de ubicación y GPS con `required="false"`.
- Al iniciar la app (`MainActivity`) se muestra el **diálogo nativo** del sistema. Si ya estaba concedido, no se vuelve a preguntar.
- La ubicación **se lee**, no solo se solicita: `UbicacionRepository` usa `LocationManager` (red y, si hay permiso preciso, GPS) y `Geocoder` para obtener región y país; si no hay geocodificador muestra las coordenadas. `UbicacionViewModel` solo mantiene el estado (`Verificando`, `NoEnUso`, `EnUso`) y el motivo cuando no se puede usar (`ProblemaUbicacion`). `IndicadorUbicacion` lo muestra en Home.
- Se vuelve a consultar al volver a la app (por ejemplo desde Ajustes) y cuando se enciende o apaga la ubicación del dispositivo (`ObservarUbicacion`).

| Situación | Qué muestra Home |
|---|---|
| Permiso rechazado, el sistema aún permite pedirlo (`shouldShowRequestPermissionRationale`) | Explicación breve y botones **Permitir** (vuelve a pedir) y **Abrir ajustes** |
| Permiso rechazado de forma definitiva | Explicación breve y botón **Abrir ajustes** (`ACTION_APPLICATION_DETAILS_SETTINGS`) |
| Ubicación del dispositivo desactivada | Explicación breve y botón **Activar ubicación** (`ACTION_LOCATION_SOURCE_SETTINGS`) |
| Sin ubicación disponible | Texto "Tu ubicación no está en uso" |
| Ubicación obtenida | "Ubicación actual: región, país" |

- La ubicación solo se usa para mostrarla en pantalla; no se guarda ni se envía a ningún servidor. Es también la base del futuro leaderboard por país.

### Voz
- `TextToSpeech` con declaración `<queries>` para el servicio TTS (necesaria desde Android 11).

---

## 8. Arquitectura

Patrón **MVVM** con una sola actividad y navegación por eventos.

```
app/src/main/java/com/example/turkids/
├── MainActivity.kt             arma las dependencias y la fábrica de ViewModels; pide el permiso de ubicación
├── model/
│   ├── BancoDeContenido.kt     unidades, palabras y generación de preguntas
│   ├── IdiomaPractica.kt       TURCO / ESPANOL
│   ├── LeccionUiState.kt       estado de una lección
│   ├── Palabra.kt              Palabra, Unidad, Pregunta
│   ├── Progreso.kt             perfiles de prueba, ProgresoUiState, LeccionGuardada, calcularEstrellas
│   ├── UbicacionEstado.kt      Verificando / NoEnUso / EnUso y ProblemaUbicacion
│   ├── UsuarioUiState.kt       estado, campos y errores del formulario
│   └── ValidadorUsuario.kt     reglas de validación (sin dependencias de Android)
├── navigation/
│   ├── Screen.kt               rutas (sealed class)
│   └── NavigationEvent.kt      NavigateTo, PopBackStack, NavigateUp
├── repository/
│   ├── ProgresoDataStore.kt    instancia única de DataStore y migración desde SharedPreferences
│   ├── ProgresoRepository.kt   lecturas con Flow, escrituras suspend
│   └── UbicacionRepository.kt  LocationManager, Geocoder y permisos
├── viewmodel/
│   ├── MainViewModel.kt        emite eventos de navegación
│   ├── LeccionViewModel.kt     lógica de la lección
│   ├── ProgresoViewModel.kt    perfil, idioma y estrellas (StateFlow)
│   ├── UsuarioViewModel.kt     estado del registro; delega las reglas en ValidadorUsuario
│   ├── UbicacionViewModel.kt   estado de la ubicación
│   └── TurKidsViewModelFactory.kt   entrega a cada ViewModel sus dependencias
└── ui/
    ├── screens/                7 pantallas + IndicadorUbicacion
    ├── theme/                  Color, Theme (colores dinámicos en Android 12+), Type
    └── utils/
        ├── Narrador.kt             TextToSpeech y rememberNarrador()
        ├── AjustesDelDispositivo.kt   abrir Ajustes de la app / de ubicación
        ├── ObservarUbicacion.kt    reconsulta al volver a la app
        └── WindowSizeUtils.kt      obtenerWindowSizeClass()
```

### Dependencias entre capas
- Las pantallas solo conocen ViewModels y modelos. Los ViewModels solo reciben repositorios (o el validador) por constructor; nunca los crean.
- `MainActivity` es la raíz de composición: crea `ProgresoRepository`, `UbicacionRepository` y `ValidadorUsuario`, y los entrega mediante `TurKidsViewModelFactory`.

### Navegación
- Las pantallas no usan el `NavController` directamente: llaman a `MainViewModel.navigateTo(...)`, que emite un `NavigationEvent` por un `SharedFlow`.
- `MainActivity` recoge los eventos (`collectLatest`) y los ejecuta sobre el `NavController`, con soporte de `popUpTo` (inclusive) y `launchSingleTop`.

| Ruta | Valor |
|---|---|
| Practica (inicio) | `practica` |
| Home | `home` |
| Acerca de | `acerca_de` |
| Registro | `registro` |
| ResumenRegistro | `resumen_registro` |
| Leccion | `leccion/{unidadId}` |
| Resumen | `resumen/{aciertos}/{total}` |

### Animaciones
- Transiciones entre pantallas: deslizamiento horizontal con fundido para la lección, vertical para el resumen, y fundidos para el resto.
- Aparición escalonada de tarjetas de unidad y de las opciones de respuesta.
- Escala al presionar tarjetas y botones, y cambio animado de color en las opciones.
- Confeti dibujado en `Canvas` en el resumen (40 partículas, 2,2 s).

---

## 9. Validaciones del formulario de registro

Las reglas están en `model/ValidadorUsuario.kt` (sin dependencias de Android, por eso se prueban con JUnit). `UsuarioViewModel` solo guarda el estado y llama al validador.

| Campo | Regla | Mensaje |
|---|---|---|
| Nombre | No vacío | Campo obligatorio |
| Correo | Formato de correo (expresión regular equivalente a `Patterns.EMAIL_ADDRESS`) | Correo inválido |
| Contraseña | Mínimo 6 caracteres | Debe tener al menos 6 caracteres |
| Dirección | No vacía | Campo obligatorio |
| Términos | Debe estar marcado | Debes aceptar los términos |

**Retroalimentación visual**
- Cada campo lleva un ícono (`Person`, `Email`, `Lock`, `LocationOn`). Si tiene error, el ícono se tiñe de rojo y aparece un ícono `Warning` al final, además del mensaje.
- La contraseña tiene un botón para mostrar u ocultar el texto (`Visibility` / `VisibilityOff`, de `material-icons-extended`).
- El error de un campo aparece solo después de salir de él por primera vez, o de pulsar **Registrar** (que marca todos los campos). Desde ese momento se actualiza mientras se escribe. Los términos se marcan como visitados al tocar la casilla.

---

## 10. Configuración de compilación

| Elemento | Versión |
|---|---|
| Android Gradle Plugin | 9.2.1 |
| Gradle (wrapper) | 9.4.1 |
| Kotlin | 2.2.10 (plugin `kotlin.compose`; sin `kotlin.android`, AGP 9 lo trae integrado) |
| compileSdk / targetSdk / minSdk | 37 / 36 / 24 |
| Java | 11 |
| Compose BOM | 2026.02.01 |
| Material 3 window size class | 1.4.0 |
| Navigation Compose | 2.9.8 |
| Lifecycle (runtime y viewmodel-compose) | 2.10.0 |
| Activity Compose | 1.13.0 |
| Core KTX | 1.19.0 |
| Coroutines | 1.10.2 |
| DataStore Preferences | 1.1.7 |
| Pruebas | JUnit 4.13.2, AndroidX JUnit 1.3.0, Espresso 3.7.0 |

- Todas las dependencias se declaran en `gradle/libs.versions.toml` (catálogo de versiones).
- `app/build.gradle.kts` usa `material-icons-core` para la mayoría de los íconos (Menu, Home, Info, ArrowBack, Person, Email, Lock, LocationOn, Warning) y `material-icons-extended` solo para `Visibility` y `VisibilityOff` (el botón de la contraseña).
- `local.properties` no se versiona: cada PC genera el suyo con la ruta del SDK.

---

## 11. Manifest y recursos

- Una sola actividad, `MainActivity`, con `MAIN` + `LAUNCHER`, `exported="true"`, `launchMode="singleTop"` y `windowSoftInputMode="adjustResize"`.
- Ícono de la app: logo TURKIDS (gato con fez). Ícono adaptable (fondo `#F7F8F1` y primer plano en `drawable-nodpi/ic_launcher_foreground.png`), más versiones raster `ic_launcher` e `ic_launcher_round` para API 24 y 25. No tiene ícono monocromático.
- `drawable/logo.png`: el logo que se muestra dentro de la app (Practica y Home).
- Tema: `Theme.TurKids` (padre `android:Theme.Material.Light.NoActionBar`) y `enableEdgeToEdge()`.

---

## 11.1 Pruebas unitarias

Están en `app/src/test/java/com/example/turkids/` y no necesitan emulador (JUnit 4, solo lógica en Kotlin).

| Archivo | Qué comprueba |
|---|---|
| `ValidadorUsuarioTest` | Cada regla del formulario (nombre, correo, contraseña, dirección, términos) y el formulario completo |
| `ProgresoTest` | `calcularEstrellas`: 0, 1, 2 y 3 estrellas, sin preguntas y totales impares |
| `BancoDeContenidoTest` | `generarPreguntas`: 3 opciones, sin repetidas, incluye la correcta, misma unidad, mismo idioma, misma semilla = misma lección |
| `UbicacionEstadoTest` | Textos de cada estado, incluido "Tu ubicación no está en uso" |

**Cómo ejecutarlas**
- Android Studio: clic derecho sobre la carpeta `app/src/test` → *Run Tests in 'test'*.
- Terminal, desde la raíz del proyecto:
  ```bash
  ./gradlew testDebugUnitTest        # Windows: gradlew.bat testDebugUnitTest
  ```
  El reporte queda en `app/build/reports/tests/testDebugUnitTest/index.html`.

---

## 12. Cómo se llegó a este proyecto

El proyecto es la **unificación** de dos repositorios del equipo:

| Origen | Qué aportó |
|---|---|
| **TURKIDS_II** (repo de la compañera) | Toda la app funcional: unidades, preguntas, validación, estrellas, audio, registro, animaciones, SharedPreferences. Es la base del código. |
| **AppTraductor_Grupo** (repo propio) | Configuración de Gradle y catálogo de versiones. No tenía lógica de preguntas ni de turco; sus pantallas de registro e inicio estaban duplicadas por las de TURKIDS. |

**Limpieza y cambios al unificar**
- Se eliminaron los comentarios explicativos y de plantilla en `.kt` y `.xml`; quedaron solo comentarios cortos de reglas del negocio.
- Las tres versiones de la pantalla de inicio (compacta, mediana y expandida) se fusionaron en una que elige 1, 2 o 3 columnas.
- `ResumenScreen` reutiliza `calcularEstrellas`, para tener la regla de estrellas en un solo lugar.
- `utils.kt` pasó a `ui/utils/WindowSizeUtils.kt`.
- Se usaron las versiones de AGP y Gradle de AppTraductor (más antiguas que las de TURKIDS: 9.4.1 y 9.6.0), con `targetSdk` 36 en vez de 37.

**Mejoras posteriores**
1. Pantalla inicial para elegir idioma de práctica (filtra las preguntas).
2. Solicitud y lectura de la ubicación (con ayuda para activar permiso o ubicación).
3. Retoma de la lección en curso e idioma persistidos (ahora con DataStore).
4. Ajustes del manifest, ícono propio y detención del audio al salir de la app.

---

## 13. Repositorio y Git

| Dato | Valor |
|---|---|
| Repositorio de trabajo | `https://github.com/corocedok/TURKIDS_II.git` (de la compañera) |
| Rama principal | `master` |
| Rama del trabajo unificado | `develop` (separada, sin mezclar a `master`) |
| Otras ramas | `feature/progreso-perfil` (PR #3), `feature/home-screen`, `feature/registro-formulario` |
| Repo propio anterior | `Safari-ux/AppTraductor_Grupo` (rama `main`) |

- La rama `develop` se creó a partir de `master` y se subió con `git push -u origin develop`.
- Decisión: mantenerla **separada** hasta probarla. No usar **Merge pull request**.
- `feature/home-screen` tenía 1 commit que `master` no tenía y la unificación no lo incluye. Conviene revisarlo.
- Los cambios de la última iteración (idioma, ubicación, retoma, ícono) pueden no estar subidos aún: revisar con `git status`.
- En el repo de la compañera hay archivos de `.idea/` y `.kotlin/` versionados. Normalmente no se suben, y conviene hablarlo con ella.

### Flujo para subir cambios (Git Bash, dentro de la carpeta del repo)
```bash
git status
git add .
git commit -m "descripción del cambio"
git push
```
Si el `push` da error 403, falta permiso de escritura en ese repo (agregar como colaborador en Settings → Collaborators).

---

## 14. Pendientes y riesgos

### Pruebas por hacer
- [ ] Sync de Gradle y compilación sin errores.
- [ ] Jugar cada unidad completa en **Turco** y en **Español** (comprobar que no se mezclan).
- [ ] Probar la segunda oportunidad al fallar y el resumen con confeti.
- [ ] Audio: escuchar en turco y en español (puede faltar la voz instalada en el emulador).
- [ ] Cerrar la app a mitad de una lección, volver a abrirla y retomar.
- [ ] Cerrar y abrir con estrellas ya guardadas.
- [ ] Diálogo de ubicación: aceptar (preciso y aproximado) y rechazar.
- [ ] Rechazar el permiso una vez (debe aparecer **Permitir** y **Abrir ajustes**) y dos veces (solo **Abrir ajustes**); comprobar que al volver desde Ajustes se actualiza.
- [ ] Apagar la ubicación del dispositivo (debe aparecer **Activar ubicación**) y volver a encenderla.
- [ ] Validaciones del registro: el error no aparece al escribir la primera letra, sí al salir del campo y al pulsar **Registrar**; probar el botón de mostrar/ocultar contraseña.
- [ ] Actualizar la app instalada con datos de la versión anterior y comprobar que se conservan estrellas, perfil e idioma (migración a DataStore).
- [ ] Ejecutar `./gradlew testDebugUnitTest`.
- [ ] Vista en tablet (1, 2 y 3 columnas) y en horizontal.

### Puntos abiertos
- **Ícono en el cajón de aplicaciones:** se reportó que no se veía. Pendiente de confirmar si aparece al deslizar hacia arriba en el emulador y, si no, revisar Logcat (`FATAL EXCEPTION`) y la pestaña Run. Se recomienda desinstalar la app antes de volver a ejecutar para refrescar el ícono.
- **Apertura automática al ejecutar:** es el comportamiento normal de Android Studio. Se desactiva en Run → Edit Configurations → Launch Options → Launch: Nothing.
- Revisar con la compañera el commit de `feature/home-screen`.
- Decidir con ella cómo y cuándo llevar `develop` a `master`.

### Limitaciones conocidas
- **No hay login:** el progreso es por perfil ficticio (Prueba 1 / Prueba 2) y vive solo en el dispositivo.
- Si se desinstala la app o se borran sus datos, se pierde el progreso.
- La ubicación se lee y se muestra, pero todavía no se envía a ningún lado (el leaderboard por país requiere un backend).
- Un solo punto de retoma por perfil: empezar otra unidad reemplaza el anterior.
- El contenido es mínimo (3 unidades de 4 palabras).

### Ideas futuras
- Leaderboard por país usando la ubicación (requiere un backend).
- Más unidades y contenido definido con el especialista.
- Cuentas de usuario y respaldo del progreso.
- Ícono monocromático para los íconos temáticos de Android 13+.
