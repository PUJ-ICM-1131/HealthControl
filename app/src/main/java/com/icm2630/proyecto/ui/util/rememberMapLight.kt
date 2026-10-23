package com.icm2630.proyecto.ui.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/** No necesita permisos. Escucha exclusivamente mientras el mapa está en primer plano. */
@Composable
fun rememberMapLight(): IluminacionMapa {
    val context = LocalContext.current.applicationContext
    val owner = LocalLifecycleOwner.current
    val manager = remember(context) { context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager }
    val sensor = remember(manager) { manager?.getDefaultSensor(Sensor.TYPE_LIGHT) }
    var estado by remember(sensor) { mutableStateOf(IluminacionMapa(sensorDisponible = sensor != null)) }

    DisposableEffect(manager, sensor, owner) {
        var registrado = false
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (registrado && event.sensor.type == Sensor.TYPE_LIGHT) {
                    event.values.firstOrNull()?.let { estado = actualizarIluminacionMapa(estado, it) }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        fun iniciar() {
            if (!registrado && manager != null && sensor != null) {
                registrado = manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
                estado = if (registrado) estado.copy(sensorDisponible = true) else IluminacionMapa()
            }
        }
        fun detener() {
            registrado = false
            manager?.unregisterListener(listener)
        }
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> iniciar()
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> detener()
                else -> Unit
            }
        }
        owner.lifecycle.addObserver(observer)
        if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) iniciar()
        onDispose {
            owner.lifecycle.removeObserver(observer)
            detener()
        }
    }
    return estado
}