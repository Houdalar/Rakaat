package com.example.rakaat.ui.components

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.rakaat.data.BluetoothManager

@SuppressLint("MissingPermission")
@Composable
fun BluetoothDevicesScreen(
    devices: List<BluetoothDevice>,
    onDeviceClick: (BluetoothDevice) -> Unit
) {
    LazyColumn {
        items(devices) { device ->
            val isConnected = BluetoothManager.currentlyConnectedDeviceAddress == device.address
            val statusText = if (isConnected) "Connected" else ""
            val backgroundColor = if (isConnected) Color.Green else Color.Transparent

            Card(

                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .clickable { onDeviceClick(device) },
                        colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.8f),
            )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = device.name ?: "Unknown Device", color = Color.White)
                    Surface(
                        color = backgroundColor,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = statusText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
