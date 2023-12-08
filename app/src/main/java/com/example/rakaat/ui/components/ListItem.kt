package com.example.rakaat.ui.components

import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@Composable
fun BluetoothDevicesScreen(
    pairedDevices: List<BluetoothDevice>,
    nonPairedDevices: List<BluetoothDevice>,
    onDeviceClick: (BluetoothDevice) -> Unit
) {
    LazyColumn {
        // Paired devices section
        if (pairedDevices.isNotEmpty()) {
            item {
                Text(
                    text = "Paired Devices",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            items(pairedDevices) { device ->
                ListItem(device = device, onClick = { onDeviceClick(device) })
                Divider()
            }
        }

        // Non-paired devices section
        if (nonPairedDevices.isNotEmpty()) {
            item {
                Text(
                    text = "Available Devices",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            items(nonPairedDevices) { device ->
                ListItem(device = device, onClick = { onDeviceClick(device) })
                Divider()
            }
        }
    }
}

@Composable
fun ListItem(device: BluetoothDevice, onClick: () -> Unit) {
    val context = LocalContext.current

    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
        val displayName = device.name ?: device.address
        Text(
            text = displayName,
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(8.dp)
        )
    }
}