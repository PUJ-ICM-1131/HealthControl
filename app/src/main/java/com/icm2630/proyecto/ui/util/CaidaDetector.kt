package com.icm2630.proyecto.ui.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * SensorEventListener que monitorea el acelerómetro en segundo plano de la UI.
 * Detecta caídas reales del usuario mediante una secuencia de dos fases:
 * 1. Caída libre (descenso brusco de la aceleración < 5 m/s²).
 * 2. Impacto inmediato (pico de aceleración > 25 m/s² dentro del segundo siguiente).
 * De esta manera se evitan falsas alarmas provocadas por sacudidas o giros del teléfono.
 */
class CaidaDetector(
    context: Context,
    private val onCaidaDetectada: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val acelerometro = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var ultimoTiempoImpacto: Long = 0
    private var tiempoCaidaLibre: Long = 0

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
        val ahora = System.currentTimeMillis()

        // 1. Fase de caída libre / baja gravedad
        if (aceleracionTotal < 5.0f) {
            tiempoCaidaLibre = ahora
        }

        // 2. Fase de impacto
        val esImpactoTrasCaida = (aceleracionTotal > 25.0f) && ((ahora - tiempoCaidaLibre) in 1..1000)
        val esImpactoExtremo = aceleracionTotal > 38.0f

        if ((esImpactoTrasCaida || esImpactoExtremo) && (ahora - ultimoTiempoImpacto) > 6000) {
            ultimoTiempoImpacto = ahora
            onCaidaDetectada()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
