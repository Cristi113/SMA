package com.mymediashelf.app.ui.utils

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

@Composable
fun rememberShakeDetector(
    onShake: () -> Unit,
    threshold: Float = 30f
): ShakeDetector {
    val context = LocalContext.current
    val sensorManager = remember {
        context.getSystemService(android.content.Context.SENSOR_SERVICE) as SensorManager
    }

    val detector = remember {
        ShakeDetector(
            sensorManager = sensorManager,
            onShake = onShake,
            threshold = threshold
        )
    }

    DisposableEffect(Unit) {
        detector.start()
        onDispose {
            detector.stop()
        }
    }

    return detector
}

class ShakeDetector(
    private val sensorManager: SensorManager,
    private val onShake: () -> Unit,
    private val threshold: Float = 200f
) : SensorEventListener {

    private var lastUpdate: Long = 0
    private var lastX: Float = 0f
    private var lastY: Float = 0f
    private var lastZ: Float = 0f
    private var shakeCount: Int = 0
    private var lastShakeTime: Long = 0
    private val SHAKE_SLOP_TIME_MS = 800
    private val MIN_SHAKE_COUNT = 6

    fun start() {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val currentTime = System.currentTimeMillis()
            if ((currentTime - lastUpdate) > 150) {
                val diffTime = currentTime - lastUpdate
                lastUpdate = currentTime

                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val deltaX = x - lastX
                val deltaY = y - lastY
                val deltaZ = z - lastZ

                val acceleration = Math.sqrt((deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ).toDouble()).toFloat()
                val speed = acceleration / diffTime * 10000

                if (currentTime - lastShakeTime > SHAKE_SLOP_TIME_MS) {
                    shakeCount = 0
                }

                if (speed > threshold) {
                    shakeCount++
                    lastShakeTime = currentTime

                    if (shakeCount >= MIN_SHAKE_COUNT) {
                        onShake()
                        shakeCount = 0
                    }
                }

                lastX = x
                lastY = y
                lastZ = z
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}