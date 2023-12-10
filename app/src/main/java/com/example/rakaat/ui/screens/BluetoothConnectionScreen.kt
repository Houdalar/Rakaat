package com.example.rakaat.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.rakaat.data.BluetoothManager
import com.example.rakaat.ui.components.BluetoothDevicesScreen
import kotlinx.coroutines.launch
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.rakaat.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothConnectionScreen(
    requestPermissionLauncher: ActivityResultLauncher<Array<String>>,
    navController: NavController,
    onConnected: () -> Unit
) {
    val context = LocalContext.current
    val devices by BluetoothManager.devices.observeAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Black),
                title = { Text("Bluetooth Connections" , color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back",tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { BluetoothManager.startDiscovery(context) }) {
                        Icon(Icons.Filled.Scanner, contentDescription = "Scan",tint = Color.White           )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            // Background Image
            Image(
                painter = painterResource(id = R.drawable.background_image),
                contentDescription = "Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Device List using BluetoothDevicesScreen
            BluetoothDevicesScreen(
                devices = devices,
                onDeviceClick = { device ->
                    coroutineScope.launch {
                        if (BluetoothManager.currentlyConnectedDeviceAddress == device.address) {
                            BluetoothManager.disconnectCurrentDevice(context)
                        } else {
                            BluetoothManager.connectSocket(device, context) { isConnected ->
                                if (isConnected) {
                                    onConnected()
                                }
                            }
                        }
                    }
                }
            )
        }
    }

    LaunchedEffect(key1 = BluetoothManager.currentlyConnectedDeviceAddress) {
        // Trigger a recomposition when the connected device address changes
    }

}

