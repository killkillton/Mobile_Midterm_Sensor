package com.example.midterm_mobile.location

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.midterm_mobile.ui.theme.Midterm_MobileTheme

data class LocationPermissionState(
    val isGranted: Boolean,
    val isFineGranted: Boolean,
    val isCoarseGranted: Boolean,
    val requestPermission: () -> Unit
)

@Composable
fun rememberLocationPermissionState(
    onPermissionResult: (Boolean) -> Unit = {}
): LocationPermissionState {
    val context = LocalContext.current
    var isFineGranted by remember {
        mutableStateOf(context.hasFineLocationPermission())
    }
    var isCoarseGranted by remember {
        mutableStateOf(context.hasCoarseLocationPermission())
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        isFineGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true || context.hasFineLocationPermission()
        isCoarseGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true || context.hasCoarseLocationPermission()
        val granted = isFineGranted || isCoarseGranted
        onPermissionResult(granted)
    }

    val isGranted = isFineGranted || isCoarseGranted

    return LocationPermissionState(
        isGranted = isGranted,
        isFineGranted = isFineGranted,
        isCoarseGranted = isCoarseGranted,
        requestPermission = {
            launcher.launch(LocationPermissionUtils.locationPermissions)
        }
    )
}

@Composable
fun LocationPermissionRequester(
    modifier: Modifier = Modifier,
    onPermissionGranted: () -> Unit = {},
    content: @Composable () -> Unit = {}
) {
    val permissionState = rememberLocationPermissionState { granted ->
        if (granted) {
            onPermissionGranted()
        }
    }

    LaunchedEffect(permissionState.isGranted) {
        if (permissionState.isGranted) {
            onPermissionGranted()
        }
    }

    if (permissionState.isGranted) {
        content()
    } else {
        LocationPermissionCard(
            modifier = modifier,
            onRequestPermission = permissionState.requestPermission
        )
    }
}

@Composable
fun LocationPermissionCard(
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = "Location Icon",
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Location Access Required",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This application needs location permissions to provide location-based features and services.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Grant Location Permission")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LocationPermissionCardPreview() {
    Midterm_MobileTheme {
        LocationPermissionCard(onRequestPermission = {})
    }
}
