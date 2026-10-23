package com.example.midterm_mobile

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.midterm_mobile.location.LocationPermissionRequester
import com.example.midterm_mobile.sensors.rememberLightSensorState
import com.example.midterm_mobile.sensors.rememberLocationSpeedState
import com.example.midterm_mobile.sensors.rememberShakeDetectorState
import com.example.midterm_mobile.ui.theme.Midterm_MobileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Midterm_MobileTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val lux = rememberLightSensorState()
    val isShaking = rememberShakeDetectorState()

    val isDarkEnvironment = lux < 50
    val targetBackgroundColor = if (isDarkEnvironment) {
        Color(0xFF121212) 
    } else if (lux > 1000) {
        Color(0xFFFFF9C4) 
    } else {
        MaterialTheme.colorScheme.background
    }

    val backgroundColor by animateColorAsState(
        targetValue = targetBackgroundColor, 
        animationSpec = tween(1000),
        label = "backgroundColor"
    )

    val contentColor = if (isDarkEnvironment) Color.White else Color.Black

    val isSystemDark = isSystemInDarkTheme()
    val isDarkBg = if (isDarkEnvironment) {
        true
    } else if (lux > 1000) {
        false
    } else {
        isSystemDark
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !isDarkBg
            insetsController.isAppearanceLightNavigationBars = !isDarkBg
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = backgroundColor
    ) { innerPadding ->
        LocationPermissionRequester(
            modifier = Modifier.padding(innerPadding)
        ) {
            val speed = rememberLocationSpeedState(hasPermission = true)
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Sensor & Location Dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                AnimatedVisibility(visible = isShaking) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = "Shake Detected")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Shake Detected!",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DashboardCard(
                        title = "Ambient Light",
                        value = "${lux.toInt()} lx",
                        icon = Icons.Default.Lightbulb,
                        modifier = Modifier.weight(1f)
                    )
                    
                    DashboardCard(
                        title = "Travel Speed",
                        value = String.format(java.util.Locale.getDefault(), "%.2f m/s", speed),
                        subtitle = "GPS: Active",
                        icon = Icons.Default.Speed,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "Learn About Sensors",
                    style = MaterialTheme.typography.titleLarge,
                    color = contentColor,
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 4.dp)
                        .align(Alignment.Start)
                )

                EducationalCard(
                    title = "Light Sensors in Heart Rate Monitors",
                    description = "Many fitness trackers use light sensors (photoplethysmography or PPG) to measure heart rate. They shine a green LED onto the skin and a light sensor measures the amount of light reflected back. Since blood absorbs green light, the reflection changes with your pulse as blood volume increases and decreases.",
                    icon = Icons.Default.Info
                )

                EducationalCard(
                    title = "Magnetic Sensors in Digital Compasses",
                    description = "Your phone's digital compass relies on a magnetometer. It often uses the Hall effect to measure the Earth's magnetic field in three dimensions. By calculating the strength and direction of the magnetic field, the sensor can determine which way is North, helping you navigate accurately.",
                    icon = Icons.Default.LocationOn
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EducationalCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
