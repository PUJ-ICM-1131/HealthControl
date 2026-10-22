package com.icm2630.proyecto.ui.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * SensorEventListener que monitorea el acelerómetro en segundo plano de la UI.
 * Detecta sacudidas o impactos bruscos que simulen una caída del usuario (Titular).
 */
class CaidaDetector(
    context: Context,
    private val onCaidaDetectada: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val acelerometro = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var ultimoTiempoImpacto: Long = 0

    fun iniciar() {
        acelerometro?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun detener() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Aceleración total en m/s^2 (gravedad normal = 9.8 m/s^2)
        val aceleracionTotal = sqrt(x * x + y * y + z * z)

        // Umbral de movimiento brusco (~38 m/s^2 o aproximadamente 3.9g, requiere sacudida fuerte)
        val umbralImpacto = 38.0f
        val ahora = System.currentTimeMillis()

        // Evitar múltiples disparos seguidos (mínimo 6 segundos entre alertas)
        if (aceleracionTotal > umbralImpacto && (ahora - ultimoTiempoImpacto) > 6000) {
            ultimoTiempoImpacto = ahora
            onCaidaDetectada()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
