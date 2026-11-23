package com.icm2630.proyecto.ui.screens

import com.icm2630.proyecto.ui.util.rememberLocationPermissionRequest
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Place
import androidx.compose.ui.text.font.FontWeight
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
import com.icm2630.proyecto.ui.viewmodel.SeleccionMapa
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
    onVolverAMiPerfil: () -> Unit = {},
    citaId: String? = null,
    titularCitaUid: String? = null,
    onVolverCita: () -> Unit = {}
) {
    val oscuroSistema = isSystemInDarkTheme()
    val iluminacion = rememberMapLight()
    val oscuro = usarMapaOscuro(oscuroSistema, iluminacion)
    // El cambio de luz abarca el mapa, los paneles y la navegación de esta pantalla.
    HealthControlMapTheme(darkTheme = oscuro) {
        MapSystemBarsEffect(oscuro)
        MapScreenContent(perfil, onNavigate, familiar, onVolverAMiPerfil, oscuro, oscuroSistema, iluminacion,
            citaId, titularCitaUid, onVolverCita)
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
    iluminacion: IluminacionMapa,
    citaId: String?,
    titularCitaUid: String?,
    onVolverCita: () -> Unit
) {
    val context = LocalContext.current

    val personasVinculadas = perfil.acompanantes
    val model: MapViewModel = viewModel(
        key = "mapa:${perfil.codigoVinculacion}:${titularCitaUid.orEmpty()}:${citaId.orEmpty()}"
    )
    val token = stringResource(R.string.mapbox_access_token).trim()
    val snackbar = remember { SnackbarHostState() }
    val state by model.uiState.collectAsStateWithLifecycle()
    val personaConsultada = state.personasEnMapa
        .find { it.persona.uid == state.personaSeleccionadaUid }?.persona
    val telefonoAcompanado = personaConsultada?.telefono?.takeIf { it.isNotBlank() }

    LaunchedEffect(model, citaId, titularCitaUid) {
        if (citaId != null && titularCitaUid != null) model.observarCita(titularCitaUid, citaId)
    }
    LaunchedEffect(model, personasVinculadas, familiar?.uid, citaId) {
        if (citaId == null) {
            model.observarPersonas(personasVinculadas)
            familiar?.uid?.let(model::seleccionarPersona)
        }
    }

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
                rutaInicio = Routes.Home
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (citaId != null) {
                    IconButton(onClick = onVolverCita) {
                        Icon(androidx.compose.material.icons.Icons.AutoMirrored.Outlined.ArrowBack, "Volver a la cita")
                    }
                }
                Text(if (citaId != null) "Ubicación de la cita" else "Mi mapa", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Icon(
                    if (oscuro) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                    contentDescription = if (oscuro) "Tema oscuro automático" else "Tema claro automático",
                    modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (familiar != null && citaId == null) {
                    TextButton(onClick = onVolverAMiPerfil) { Text("Mi perfil") }
                }
                if (telefonoAcompanado != null) {
                    IconButton(onClick = {
                        try {
                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(telefonoAcompanado)}")))
                        } catch (_: ActivityNotFoundException) {
                            Toast.makeText(context, "No hay una aplicación de llamadas disponible.", Toast.LENGTH_SHORT).show()
                        }
                    }) { Icon(Icons.Outlined.Call, "Llamar a ${personaConsultada?.nombre.orEmpty()}") }
                }
            }
            if (!token.startsWith("pk.")) {
                Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("El mapa aún no está configurado. Añade el token público de Mapbox indicado en la guía del proyecto.")
                }
            } else {
                key(model) {
                    MapContent(model, token, permisoInicial = perfil.permisoUbicacion, oscuro = oscuro, enCita = citaId != null)
                }
            }
        }
    }
}

@OptIn(MapboxExperimental::class)
@Composable
private fun MapContent(model: MapViewModel, token: String, permisoInicial: Boolean, oscuro: Boolean, enCita: Boolean = false) {
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
    var expandido by rememberSaveable { mutableStateOf(false) }
    var seccion by rememberSaveable { mutableIntStateOf(0) }
    var mostrarBusqueda by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.personaSeleccionadaUid) {
        if (state.personaSeleccionadaUid != null) {
            seccion = 0
            expandido = true
        }
    }
    LaunchedEffect(state.cita?.id, state.cita?.institucion, state.cita?.direccion, state.cita?.modalidad) {
        val cita = state.cita
        if (enCita && cita != null) {
            seccion = 0
            expandido = true
            mostrarBusqueda = cita.modalidad == com.icm2630.proyecto.data.model.ModalidadCita.PRESENCIAL &&
                com.icm2630.proyecto.data.model.UbicacionCitas.punto(cita) == null
            if (mostrarBusqueda && state.consulta.trim().length >= 3) model.buscarDireccion()
        }
    }
    LaunchedEffect(state.ruta) {
        if (enCita && state.ruta != null) {
            expandido = false
            mostrarBusqueda = false
        }
    }
    var centradoInicial by rememberSaveable { mutableStateOf(false) }
    var altoBusqueda by remember { mutableIntStateOf(0) }
    var altoControles by remember { mutableIntStateOf(0) }

    val pedirUbicacion = rememberLocationPermissionRequest { concedido ->
        preciso = tienePermiso(Manifest.permission.ACCESS_FINE_LOCATION)
        permitido = concedido
        permisoRechazado = !concedido
        if (!concedido) model.cancelarRutaCitaPendiente()
        usarUbicacion = concedido
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
            !permitido -> pedirUbicacion()
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
    LaunchedEffect(state.destino, altoControles, altoBusqueda) {
        if (enCita && state.ruta == null) state.destino?.let {
            viewport.setCameraOptions {
                center(it.aMapbox())
                padding(EdgeInsets(altoBusqueda.toDouble() + 24, 24.0, altoControles.toDouble() + 24, 24.0))
                zoom(16.0)
            }
            centradoInicial = true
        }
    }
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
            // Reserva espacio para las etiquetas de los marcadores y los paneles reales.
            val superior = altoBusqueda + margen * 3
            val inferior = altoControles + margen
            val factor = ((altoMapaPx - margen * 2).coerceAtLeast(1.0) / (superior + inferior))
                .coerceAtMost(1.0)
            viewport.transitionToOverviewState(
                OverviewViewportStateOptions.Builder()
                    .geometry(LineString.fromLngLats(puntos))
                    .padding(if (enCita) EdgeInsets(0.0, 0.0, 0.0, 0.0) else EdgeInsets(
                        min(altoBusqueda + margen, altoMapaPx * 0.35), margen,
                        min(altoControles + margen, altoMapaPx * 0.45), margen))
                    .geometryPadding(if (enCita) EdgeInsets(superior * factor, margen, inferior * factor, margen)
                        else EdgeInsets(0.0, 0.0, 0.0, 0.0))
                    .maxZoom(16.0)
                    .build()
            )
        }
        LaunchedEffect(state.ruta, if (enCita) altoControles else 0, if (enCita) altoBusqueda else 0) {
            if (state.ruta != null) verRutaCompleta()
        }

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
                seccion = if (enCita && state.seleccion == SeleccionMapa.DESTINO) 0 else 1
                expandido = true
                mostrarBusqueda = false
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
                state.personasEnMapa.forEach { persona ->
                    persona.punto?.let { punto ->
                        key(persona.persona.uid) {
                            MapPointMarker(
                                punto.aMapbox(),
                                persona.persona.nombre.take(1).uppercase(),
                                persona.persona.nombre,
                                if (persona.persona.uid == state.personaSeleccionadaUid) colores.tertiary else colores.secondary,
                                if (persona.persona.uid == state.personaSeleccionadaUid) colores.onTertiary else colores.onSecondary,
                                onClick = {
                                    model.seleccionarPersona(persona.persona.uid)
                                    seccion = 0
                                    expandido = true
                                    mostrarBusqueda = false
                                }
                            )
                        }
                    }
                }
            }
        )

        Column(
            modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(12.dp)
                .onSizeChanged { altoBusqueda = it.height },
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(shape = RoundedCornerShape(20.dp), shadowElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = {
                        mostrarBusqueda = !mostrarBusqueda
                        if (mostrarBusqueda) expandido = false
                        keyboard?.hide()
                    },
                        modifier = Modifier.weight(1f)) {
                        Icon(if (mostrarBusqueda) Icons.Outlined.Close else Icons.Outlined.Search, null)
                        Spacer(Modifier.width(10.dp))
                        Text(if (mostrarBusqueda) "Cerrar búsqueda" else "Buscar un lugar",
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    IconButton(onClick = {
                        val punto = state.ubicacionActual
                        if (punto != null && permitido && servicioActivo) {
                            centradoInicial = true
                            viewport.setCameraOptions { center(punto.aMapbox()); zoom(16.0) }
                        } else activarUbicacion()
                    }) { Icon(Icons.Outlined.MyLocation, "Centrar o activar mi ubicación") }
                }
            }
            if (mostrarBusqueda) {
                MapSearchCard(
                    state = state, onConsulta = model::cambiarConsulta,
                    onBuscar = { keyboard?.hide(); model.buscarDireccion() },
                    onCentrosSalud = if (enCita) null else { { keyboard?.hide(); model.buscarCentrosSalud(token) } },
                    onElegir = {
                        keyboard?.hide()
                        mostrarBusqueda = false
                        centradoInicial = true
                        model.seleccionarPunto(it)
                        seccion = if (enCita && state.seleccion == SeleccionMapa.DESTINO) 0 else 1
                        expandido = true
                        viewport.setCameraOptions { center(it.aMapbox()); zoom(15.0) }
                    }
                )
            }
            if (errorMapa) {
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.errorContainer) {
                    Text("No se pudo cargar el mapa. Revisa tu conexión.", Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 12.dp)
                .padding(bottom = 36.dp).heightIn(max = maxHeight * 0.48f)
                .onSizeChanged { altoControles = it.height + with(density) { 36.dp.roundToPx() } },
            shadowElevation = 8.dp, shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column {
                Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    listOf(if (enCita) "Cita" else "Personas", "Ruta", "Mi posición").forEachIndexed { indice, titulo ->
                        TextButton(onClick = {
                            seccion = indice
                            expandido = true
                            mostrarBusqueda = false
                            keyboard?.hide()
                        }, modifier = Modifier.weight(1f)) {
                            Text(titulo, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                color = if (seccion == indice) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (seccion == indice) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                    IconButton(onClick = { expandido = !expandido; keyboard?.hide() }) {
                        Icon(if (expandido) Icons.Outlined.ExpandMore else Icons.Outlined.ExpandLess,
                            if (expandido) "Ocultar panel" else "Mostrar panel")
                    }
                }
                if (!expandido) {
                    val visibles = state.personasEnMapa.count { it.punto != null }
                    Text(
                        if (state.calculandoRuta) "Calculando el recorrido…"
                        else if (state.ruta != null) "Ruta trazada · abre Ruta para ver el recorrido"
                        else if (enCita) state.cita?.institucion?.ifBlank { "Lugar de la cita" } ?: "Consultando cita…"
                        else "${state.personasEnMapa.size} ${if (state.personasEnMapa.size == 1) "persona vinculada" else "personas vinculadas"} · $visibles con ubicación",
                        Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 8.dp)) {
                        when (seccion) {
                            0 -> if (enCita) {
                                com.icm2630.proyecto.ui.components.map.UbicacionCitaCard(
                                    state = state,
                                    onBuscar = {
                                        model.seleccionarObjetivo(SeleccionMapa.DESTINO)
                                        state.cita?.let { model.cambiarConsulta(com.icm2630.proyecto.data.model.UbicacionCitas.consulta(it)) }
                                        mostrarBusqueda = true
                                        expandido = false
                                    },
                                    onGuardar = model::confirmarLugarCita,
                                    onRuta = {
                                        activarUbicacion()
                                        model.rutaHaciaCita(token)
                                        seccion = 1
                                        expandido = true
                                        mostrarBusqueda = false
                                        keyboard?.hide()
                                    }
                                )
                            } else {
                                if (state.personasEnMapa.isEmpty()) {
                                    Text("Tus personas vinculadas aparecerán aquí cuando acepten la invitación.",
                                        Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
                                }
            if (state.personasEnMapa.isNotEmpty()) {
                Surface(shape = RoundedCornerShape(16.dp), shadowElevation = 2.dp) {
                    Column(Modifier.padding(10.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("Personas vinculadas", Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                            TextButton(
                                enabled = state.personasEnMapa.any { it.punto != null },
                                onClick = {
                                    val puntos = state.personasEnMapa.mapNotNull { it.punto?.aMapbox() } +
                                        listOfNotNull(state.ubicacionActual?.aMapbox())
                                    centradoInicial = true
                                    if (puntos.size == 1) {
                                        viewport.setCameraOptions { center(puntos.first()); zoom(15.0) }
                                    } else if (puntos.size > 1) {
                                        viewport.transitionToOverviewState(
                                            OverviewViewportStateOptions.Builder()
                                                .geometry(LineString.fromLngLats(puntos))
                                                .padding(EdgeInsets(
                                                    min(altoBusqueda + margen, altoMapaPx * 0.40), margen,
                                                    min(altoControles + margen, altoMapaPx * 0.40), margen
                                                ))
                                                .maxZoom(16.0).build()
                                        )
                                    }
                                }
                            ) { Text("Ver todos") }
                        }
                        Row(
                            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.personasEnMapa.forEach { persona ->
                                FilterChip(
                                    selected = state.personaSeleccionadaUid == persona.persona.uid,
                                    onClick = {
                                        model.seleccionarPersona(persona.persona.uid)
                                        persona.punto?.let {
                                            centradoInicial = true
                                            viewport.setCameraOptions { center(it.aMapbox()); zoom(16.0) }
                                        }
                                    },
                                    label = {
                                        Text(persona.persona.nombre + when {
                                            persona.cargando -> " · Cargando…"
                                            persona.punto == null -> " · Sin ubicación"
                                            else -> ""
                                        })
                                    }
                                )
                            }
                        }
                        val seleccionada = state.personasEnMapa.find {
                            it.persona.uid == state.personaSeleccionadaUid
                        }
                        if (seleccionada?.punto == null && seleccionada != null) {
                            Text(
                                if (seleccionada.cargando) "Consultando ubicación…"
                                else "Ubicación no disponible. Esta persona debe activar la opción de compartir y tener conexión.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else if (seleccionada == null) {
                            Text("Selecciona un marcador o una persona para ver su ubicación y calcular la ruta.",
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

                                state.ubicacionFamiliar?.let { persona ->
                                    UbicacionFamiliarCard(
                                        nombre = persona.nombre, momentoMillis = state.momentoFamiliarMillis,
                                        onCentrar = {
                                            centradoInicial = true
                                            viewport.setCameraOptions { center(persona.aMapbox()); zoom(16.0) }
                                            expandido = false
                                        },
                                        onRuta = {
                                            keyboard?.hide()
                                            seccion = 1
                                            model.rutaHaciaFamiliar(token)
                                        }
                                    )
                                }
                            }
                            1 -> MapRouteControls(
                                state = state, expandido = true,
                                onExpandir = { expandido = false; keyboard?.hide() },
                                onSeleccionar = { model.seleccionarObjetivo(it); mostrarBusqueda = true; expandido = false },
                                onUsarUbicacion = { model.usarMiUbicacionComoOrigen(); activarUbicacion() },
                                onModoRuta = model::cambiarModo,
                                onCalcular = {
                                    keyboard?.hide()
                                    model.calcularRuta(token)
                                    expandido = false
                                },
                                onVerRuta = { verRutaCompleta(); expandido = false },
                                onLimpiar = { if (enCita) model.restaurarDestinoCita() else model.limpiarRuta() }
                            )
                            2 -> {
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
                                    TextButton(onClick = { abrirAjustes(ubicacion = false) },
                                        modifier = Modifier.padding(horizontal = 12.dp)) {
                                        Text("Abrir ajustes de ubicación")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun PuntoMapa.aMapbox(): Point = Point.fromLngLat(longitud, latitud)

/** Última ubicación real compartida: centrar en el mapa o trazar ruta. */
@Composable
private fun UbicacionFamiliarCard(
    nombre: String,
    momentoMillis: Long?,
    onCentrar: () -> Unit,
    onRuta: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp), shadowElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 12.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "Última ubicación compartida de $nombre",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(tiempoDesde(momentoMillis), style = MaterialTheme.typography.labelSmall)
            }
            IconButton(onClick = onCentrar) {
                Icon(Icons.Outlined.Place, contentDescription = "Ver a $nombre en el mapa")
            }
            TextButton(onClick = onRuta) { Text("Ruta") }
        }
    }
}

private fun tiempoDesde(millis: Long?): String {
    if (millis == null) return "Última ubicación conocida"
    val minutos = ((System.currentTimeMillis() - millis) / 60_000L).coerceAtLeast(0L)
    return when {
        minutos < 1L -> "Actualizada hace un momento"
        minutos < 60L -> "Actualizada hace $minutos min"
        else -> "Actualizada a las " + DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(millis))
    }
}