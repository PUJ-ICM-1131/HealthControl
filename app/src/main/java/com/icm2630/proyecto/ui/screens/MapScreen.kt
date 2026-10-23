package com.icm2630.proyecto.ui.screens

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.icm2630.proyecto.R
import com.icm2630.proyecto.data.model.PerfilUsuario
import com.icm2630.proyecto.data.model.PersonaVinculada
import com.icm2630.proyecto.data.model.PuntoMapa
import com.icm2630.proyecto.data.model.TipoPerfil
import com.icm2630.proyecto.data.repository.VinculacionSimulator
import com.icm2630.proyecto.navigation.Routes
import com.icm2630.proyecto.ui.components.HealthBottomNavigation
import com.icm2630.proyecto.ui.components.map.MapRouteControls
import com.icm2630.proyecto.ui.components.map.MapPointMarker
import com.icm2630.proyecto.ui.components.map.MapSearchCard
import com.icm2630.proyecto.ui.components.map.coordenadasMapa
import com.icm2630.proyecto.ui.util.LocationUpdatesEffect
import com.icm2630.proyecto.ui.util.MapSystemBarsEffect
import com.icm2630.proyecto.ui.util.IluminacionMapa
import com.icm2630.proyecto.ui.util.rememberMapLight
import com.icm2630.proyecto.ui.util.usarMapaOscuro
import com.icm2630.proyecto.ui.theme.HealthControlMapTheme
import com.icm2630.proyecto.ui.theme.Blue100
import com.icm2630.proyecto.ui.viewmodel.MapViewModel
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.MapboxMapComposable
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.extension.compose.style.standard.MapboxStandardStyle
import com.mapbox.maps.extension.compose.style.standard.LightPresetValue
import com.mapbox.maps.extension.compose.style.standard.ThemeValue
import com.mapbox.maps.extension.compose.style.standard.rememberStandardStyleState
import com.mapbox.maps.extension.compose.style.BooleanValue
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.compose.style.MapboxStyleComposable
import com.mapbox.maps.extension.compose.style.ColorValue
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.data.OverviewViewportStateOptions
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun MapScreen(
    perfil: PerfilUsuario = PerfilUsuario(),
    onNavigate: (Routes) -> Unit = {},
    familiar: PersonaVinculada? = null,
    onVolverAMiPerfil: () -> Unit = {}
) {
    val oscuroSistema = isSystemInDarkTheme()
    val iluminacion = rememberMapLight()
    val oscuro = usarMapaOscuro(oscuroSistema, iluminacion)
    // El cambio de luz abarca el mapa, los paneles y la navegación de esta pantalla.
    HealthControlMapTheme(darkTheme = oscuro) {
        MapSystemBarsEffect(oscuro)
        MapScreenContent(perfil, onNavigate, familiar, onVolverAMiPerfil, oscuro, oscuroSistema, iluminacion)
    }
}

@Composable
private fun MapScreenContent(
    perfil: PerfilUsuario,
    onNavigate: (Routes) -> Unit,
    familiar: PersonaVinculada?,
    onVolverAMiPerfil: () -> Unit,
    oscuro: Boolean,
    oscuroSistema: Boolean,
    iluminacion: IluminacionMapa
) {
    val context = LocalContext.current
    // El flujo actual usa familiarActivo. Se conserva compatibilidad con el rol antiguo.
    val personaConsultada = familiar ?: perfil.personaVinculada
        ?.takeIf { perfil.tipoPerfil == TipoPerfil.ACOMPANANTE }
    // Mantiene la acción de llamada que tenía la pantalla del acompañante.
    val telefonoAcompanado = perfil.personaVinculada
        ?.takeIf { perfil.tipoPerfil == TipoPerfil.ACOMPANANTE && familiar == null }
        ?.let { VinculacionSimulator.telefonoParaCodigo(it.codigo) }
    val model: MapViewModel = viewModel(
        key = "mapa:${perfil.codigoVinculacion}:${personaConsultada?.codigo}"
    )
    val token = stringResource(R.string.mapbox_access_token).trim()
    val snackbar = remember { SnackbarHostState() }
    val state by model.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(model, state.mensajeError) {
        state.mensajeError?.let {
            snackbar.showSnackbar(it)
            model.limpiarError()
        }
    }
    DisposableEffect(model) {
        onDispose { model.cancelarOperaciones() }
    }

    Scaffold(
        bottomBar = {
            HealthBottomNavigation(
                currentRoute = Routes.Mapa,
                onNavigate = onNavigate,
                rutaInicio = if (perfil.tipoPerfil == TipoPerfil.ACOMPANANTE) Routes.Monitoreo else Routes.Home
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    color = Color.White
                ) {
                    Image(
                        painterResource(R.drawable.loguitouwu), contentDescription = null,
                        modifier = Modifier.size(44.dp), contentScale = ContentScale.Crop
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Tu mapa", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    Text("HealthControl", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    personaConsultada?.let {
                        Text("Consultando a ${it.nombre}", style = MaterialTheme.typography.bodySmall)
                    }
                }
                MapLightStatus(oscuro, oscuroSistema, iluminacion)
                if (familiar != null) {
                    TextButton(onClick = onVolverAMiPerfil) { Text("Mi perfil") }
                }
                if (telefonoAcompanado != null) {
                    IconButton(onClick = {
                        try {
                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(telefonoAcompanado)}")))
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(context, "No hay una aplicación de llamadas disponible.", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Outlined.Call, contentDescription = "Llamar a ${personaConsultada?.nombre.orEmpty()}")
                    }
                }
            }
            personaConsultada?.let {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        "No hay una ubicación compartida de ${it.nombre}. Puedes planear una ruta desde este celular o elegir otro origen.",
                        modifier = Modifier.padding(10.dp), style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            if (!token.startsWith("pk.")) {
                Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("El mapa aún no está configurado. Añade el token público de Mapbox indicado en la guía del proyecto.")
                }
            } else {
                key(model) {
                    MapContent(model, token, permisoInicial = perfil.permisoUbicacion, oscuro = oscuro)
                }
            }
        }
    }
}

@Composable
private fun MapLightStatus(oscuro: Boolean, oscuroSistema: Boolean, luz: IluminacionMapa) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (oscuro) Icons.Outlined.DarkMode else Icons.Outlined.LightMode, null, Modifier.size(16.dp))
            Spacer(Modifier.width(5.dp))
            Column {
                Text(
                    when {
                        oscuroSistema -> "Oscuro · sistema"
                        oscuro -> "Noche · poca luz"
                        else -> "Claro"
                    }, style = MaterialTheme.typography.labelSmall
                )
                Text(
                    luz.lux?.let { "${it.roundToInt()} lx" }
                        ?: if (luz.sensorDisponible) "Leyendo luz…" else "Tema del celular",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@OptIn(MapboxExperimental::class)
@Composable
private fun MapContent(model: MapViewModel, token: String, permisoInicial: Boolean, oscuro: Boolean) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val keyboard = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current
    val estiloClaro = stringResource(R.string.mapbox_style_claro).trim()
    val estiloOscuro = stringResource(R.string.mapbox_style_oscuro).trim()
    val estiloPublicado = if (oscuro) estiloOscuro else estiloClaro
    // Capturados fuera del contenido de Mapbox para compartir los mismos colores en controles y mapa.
    val colores = MaterialTheme.colorScheme
    val colorRuta = colores.primary
    val bordeRuta = colores.surface
    val colorDestino = colores.tertiary
    // Estas opciones solo se usan si falta la URL del modo seleccionado.
    // El estilo publicado se carga completo con MapStyle y conserva su diseño de Studio.
    val standardStyle = rememberStandardStyleState {
        configurationsState.apply {
            lightPreset = if (oscuro) LightPresetValue.NIGHT else LightPresetValue.DAY
            theme = ThemeValue.FADED
            show3dObjects = BooleanValue(false)
            showPedestrianRoads = BooleanValue(true)
            colorLand = ColorValue(if (oscuro) Color(0xFF101E30) else Color(0xFFF4F8FF))
            colorWater = ColorValue(if (oscuro) Color(0xFF1C4669) else Blue100)
            colorBuildings = ColorValue(if (oscuro) Color(0xFF253850) else Color(0xFFE1EAF5))
            colorMedical = ColorValue(if (oscuro) Color(0xFF1A3B3C) else Color(0xFFD9F8EC))
        }
    }
    val state by model.uiState.collectAsStateWithLifecycle()
    val locationManager = remember(context) {
        context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
    }
    fun tienePermiso(nombre: String) =
        ContextCompat.checkSelfPermission(context, nombre) == PackageManager.PERMISSION_GRANTED

    var preciso by remember { mutableStateOf(tienePermiso(Manifest.permission.ACCESS_FINE_LOCATION)) }
    var permitido by remember { mutableStateOf(preciso || tienePermiso(Manifest.permission.ACCESS_COARSE_LOCATION)) }
    var usarUbicacion by rememberSaveable { mutableStateOf(permisoInicial) }
    var servicioActivo by remember { mutableStateOf(LocationManagerCompat.isLocationEnabled(locationManager)) }
    var enPrimerPlano by remember {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }
    var permisoRechazado by rememberSaveable { mutableStateOf(false) }
    var errorUbicacion by remember { mutableStateOf<String?>(null) }
    var ubicacionDemorada by remember { mutableStateOf(false) }
    var errorMapa by remember { mutableStateOf(false) }
    var reintento by remember { mutableIntStateOf(0) }
    var expandido by rememberSaveable { mutableStateOf(true) }
    var centradoInicial by rememberSaveable { mutableStateOf(false) }
    var altoBusqueda by remember { mutableIntStateOf(0) }
    var altoControles by remember { mutableIntStateOf(0) }

    val permisosLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permisos ->
        preciso = permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                tienePermiso(Manifest.permission.ACCESS_FINE_LOCATION)
        permitido = preciso || permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
                tienePermiso(Manifest.permission.ACCESS_COARSE_LOCATION)
        permisoRechazado = !permitido
        usarUbicacion = permitido
    }
    DisposableEffect(lifecycleOwner) {
        val observador = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                preciso = tienePermiso(Manifest.permission.ACCESS_FINE_LOCATION)
                permitido = preciso || tienePermiso(Manifest.permission.ACCESS_COARSE_LOCATION)
                servicioActivo = LocationManagerCompat.isLocationEnabled(locationManager)
                enPrimerPlano = true
            } else if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                enPrimerPlano = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observador)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observador) }
    }
    fun abrirAjustes(ubicacion: Boolean) {
        try {
            context.startActivity(
                if (ubicacion) Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                else Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
            )
        } catch (e: ActivityNotFoundException) {
            errorUbicacion = "No se pudieron abrir los ajustes. Ábrelos desde la configuración del dispositivo."
        }
    }
    val activarUbicacion: () -> Unit = {
        usarUbicacion = true
        errorUbicacion = null
        when {
            !permitido -> permisosLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
            !servicioActivo -> abrirAjustes(ubicacion = true)
            else -> reintento++
        }
    }

    LocationUpdatesEffect(
        habilitado = usarUbicacion && permitido,
        permisoPreciso = preciso,
        reintento = reintento,
        onUbicacion = { location ->
            model.actualizarUbicacion(
                PuntoMapa(location.latitude, location.longitude, "Este celular"),
                if (location.hasAccuracy()) location.accuracy else null,
                location.time
            )
        },
        onEstadoServicio = { servicioActivo = it },
        onError = { errorUbicacion = it }
    )
    LaunchedEffect(usarUbicacion, permitido, servicioActivo, enPrimerPlano, reintento, state.ubicacionActual) {
        ubicacionDemorada = false
        if (usarUbicacion && permitido && servicioActivo && enPrimerPlano && state.ubicacionActual == null) {
            delay(20_000L)
            ubicacionDemorada = true
        }
    }
    val viewport = rememberMapViewportState {
        // Vista inicial, no una ubicación atribuida al usuario ni al familiar.
        setCameraOptions {
            center(Point.fromLngLat(-74.064163, 4.627389))
            zoom(12.0)
            pitch(0.0)
            bearing(0.0)
        }
    }
    val mapState = rememberMapState()
    LaunchedEffect(mapState) { mapState.mapLoadingErrorEvents.collect { errorMapa = true } }
    LaunchedEffect(mapState) { mapState.mapLoadedEvents.collect { errorMapa = false } }
    LaunchedEffect(state.ubicacionActual) {
        if (!centradoInicial && state.ruta == null && state.destino == null && state.origenDesdeUbicacion) {
            state.ubicacionActual?.let {
                viewport.setCameraOptions { center(it.aMapbox()); zoom(15.0) }
                centradoInicial = true
            }
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val altoMapaPx = with(density) { maxHeight.toPx().toDouble() }
        val margen = with(density) { 24.dp.toPx().toDouble() }
        fun verRutaCompleta() {
            val ruta = state.ruta ?: return
            val puntos = ruta.puntos.map { it.aMapbox() } +
                    listOfNotNull(state.origen?.aMapbox(), state.destino?.aMapbox())
            viewport.transitionToOverviewState(
                OverviewViewportStateOptions.Builder()
                    .geometry(LineString.fromLngLats(puntos))
                    .padding(EdgeInsets(
                        min(altoBusqueda + margen, altoMapaPx * 0.35), margen,
                        min(altoControles + margen, altoMapaPx * 0.45), margen
                    ))
                    .maxZoom(16.0)
                    .build()
            )
        }
        LaunchedEffect(state.ruta) { if (state.ruta != null) verRutaCompleta() }

        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = viewport,
            mapState = mapState,
            // La escala predeterminada se superpone con el buscador en la esquina superior.
            scaleBar = {},
            style = @Composable @MapboxStyleComposable {
                if (estiloPublicado.isNotEmpty()) {
                    // Respeta colores, etiquetas y objetos 3D guardados en Mapbox Studio.
                    MapStyle(style = estiloPublicado)
                } else {
                    // Respaldo nocturno hasta configurar la URL propia del estilo oscuro.
                    MapboxStandardStyle(standardStyleState = standardStyle)
                }
            },
            onMapLongClickListener = { point ->
                keyboard?.hide()
                centradoInicial = true
                model.seleccionarPunto(PuntoMapa(point.latitude(), point.longitude()))
                true
            },
            logo = { Logo(Modifier.padding(bottom = 8.dp)) },
            attribution = { Attribution(Modifier.padding(bottom = 8.dp)) },
            content = @Composable @MapboxMapComposable {
                val mostrarPuck = usarUbicacion && permitido && servicioActivo && enPrimerPlano
                MapEffect(mostrarPuck, colorRuta) { mapView ->
                    mapView.location.updateSettings {
                        enabled = mostrarPuck
                        locationPuck = createDefault2DPuck(withBearing = true)
                        puckBearing = PuckBearing.COURSE
                        puckBearingEnabled = true
                        pulsingEnabled = true
                        pulsingColor = colorRuta.toArgb()
                    }
                }
                state.ruta?.let { ruta ->
                    PolylineAnnotation(points = ruta.puntos.map { it.aMapbox() }) {
                        lineColor = colorRuta
                        lineWidth = 7.0
                        lineBorderColor = bordeRuta
                        lineBorderWidth = 2.0
                        lineJoin = LineJoin.ROUND
                        // Mantiene el contraste del recorrido también con la iluminación nocturna.
                        lineEmissiveStrength = 1.0
                    }
                }
                state.origen?.let {
                    MapPointMarker(it.aMapbox(), "A", "Origen", colorRuta, colores.onPrimary)
                }
                state.destino?.let {
                    MapPointMarker(it.aMapbox(), "B", "Destino", colorDestino, colores.onTertiary)
                }
            }
        )

        Column(
            modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(12.dp)
                .onSizeChanged { altoBusqueda = it.height },
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MapSearchCard(
                state = state,
                onConsulta = model::cambiarConsulta,
                onBuscar = { keyboard?.hide(); model.buscarDireccion() },
                onElegir = {
                    keyboard?.hide()
                    centradoInicial = true
                    model.seleccionarPunto(it)
                    viewport.setCameraOptions { center(it.aMapbox()); zoom(15.0) }
                }
            )
            Surface(
                shape = RoundedCornerShape(16.dp), shadowElevation = 2.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 12.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        val mensaje = when {
                            !usarUbicacion -> "Puedes usar la ubicación de este celular."
                            !permitido -> "Permite el acceso a la ubicación."
                            !servicioActivo -> "La ubicación del dispositivo está desactivada."
                            errorUbicacion != null -> errorUbicacion!!
                            ubicacionDemorada -> "Aún no recibimos una ubicación. Puedes elegir el origen en el mapa."
                            state.ubicacionActual == null -> "Buscando la ubicación del celular…"
                            !preciso -> "Ubicación aproximada de este celular"
                            else -> "Ubicación de este celular"
                        }
                        Text(mensaje, style = MaterialTheme.typography.bodySmall)
                        state.ubicacionActual?.let {
                            val hora = state.momentoUbicacionMillis?.let { momento ->
                                DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(momento))
                            }.orEmpty()
                            val precision = state.precisionMetros?.takeIf { p -> p.isFinite() && p >= 0 }
                                ?.let { p -> " · ±${p.roundToInt()} m" }.orEmpty()
                            Text("${coordenadasMapa(it)}$precision · $hora", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    when {
                        !usarUbicacion || !permitido || !servicioActivo || errorUbicacion != null || ubicacionDemorada -> {
                            TextButton(onClick = activarUbicacion) {
                                Text(if (!servicioActivo && permitido) "Activar" else "Usar ubicación")
                            }
                        }
                        state.ubicacionActual == null -> CircularProgressIndicator(
                            modifier = Modifier.padding(12.dp).size(20.dp), strokeWidth = 2.dp
                        )
                        else -> IconButton(onClick = {
                            state.ubicacionActual?.let { punto ->
                                centradoInicial = true
                                viewport.setCameraOptions { center(punto.aMapbox()); zoom(16.0) }
                            }
                        }) { Icon(Icons.Outlined.MyLocation, "Centrar este celular en el mapa") }
                    }
                }
            }
            if (permisoRechazado && !permitido) {
                Surface(shape = RoundedCornerShape(12.dp)) {
                    TextButton(onClick = { abrirAjustes(ubicacion = false) }) {
                        Text("Abrir ajustes para permitir la ubicación")
                    }
                }
            }
            if (errorMapa) {
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.errorContainer) {
                    Text("No se pudo cargar el mapa. Revisa internet, que el estilo esté publicado y el token de tu cuenta de Mapbox.", Modifier.padding(10.dp), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 12.dp)
                // Reserva el borde inferior para el logo y la atribución de Mapbox.
                .padding(bottom = 40.dp)
                .heightIn(max = maxHeight * 0.50f)
                .onSizeChanged { altoControles = it.height + with(density) { 40.dp.roundToPx() } }
                .clip(RoundedCornerShape(22.dp)),
            shadowElevation = 6.dp, shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                MapRouteControls(
                    state = state,
                    expandido = expandido,
                    onExpandir = { keyboard?.hide(); expandido = !expandido },
                    onSeleccionar = model::seleccionarObjetivo,
                    onUsarUbicacion = { model.usarMiUbicacionComoOrigen(); activarUbicacion() },
                    onModoRuta = model::cambiarModo,
                    onCalcular = { keyboard?.hide(); model.calcularRuta(token) },
                    onVerRuta = ::verRutaCompleta,
                    onLimpiar = model::limpiarRuta
                )
            }
        }
    }
}

private fun PuntoMapa.aMapbox(): Point = Point.fromLngLat(longitud, latitud)