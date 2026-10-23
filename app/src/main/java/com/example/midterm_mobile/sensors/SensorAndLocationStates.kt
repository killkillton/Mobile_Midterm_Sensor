package com.example.midterm_mobile.sensors

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.delay
import kotlin.math.sqrt

@Composable
fun rememberLightSensorState(): Float {
    val context = LocalContext.current
    var lux by remember { mutableFloatStateOf(0f) }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    lux = it.values[0]
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        
        lightSensor?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }
        
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }
    
    return lux
}

@Composable
fun rememberShakeDetectorState(): Boolean {
    val context = LocalContext.current
    var isShaking by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val x = it.values[0] / SensorManager.GRAVITY_EARTH
                    val y = it.values[1] / SensorManager.GRAVITY_EARTH
                    val z = it.values[2] / SensorManager.GRAVITY_EARTH
                    
                    val gForce = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                    
                    if (gForce > 2.5f) { // threshold for shake
                        isShaking = true
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        
        accelerometer?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }
        
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }
    
    // Automatically reset shake state after a short delay
    LaunchedEffect(isShaking) {
        if (isShaking) {
            delay(500)
            isShaking = false
        }
    }
    
    return isShaking
}

@SuppressLint("MissingPermission")
@Composable
fun rememberLocationSpeedState(hasPermission: Boolean): Float {
    val context = LocalContext.current
    var speed by remember { mutableFloatStateOf(0f) }
    
    DisposableEffect(context, hasPermission) {
        if (!hasPermission) return@DisposableEffect onDispose {}
        
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
            .setMinUpdateIntervalMillis(1000)
            .build()
            
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    if (location.hasSpeed()) {
                        speed = location.speed // in meters/second
                    }
                }
            }
        }
        
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        
        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
    
    return speed
}
