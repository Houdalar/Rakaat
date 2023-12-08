package com.example.rakaat.ui.screens

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.rakaat.ui.components.BluetoothDevicesScreen
import kotlinx.coroutines.launch
import com.example.rakaat.data.BluetoothViewModel
import com.example.rakaat.data.PrayerViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothConnectionScreen(
    requestPermissionLauncher: ActivityResultLauncher<Array<String>>,
    navController: NavController,
    onConnected: () -> Unit
) {
    val viewModel: BluetoothViewModel = hiltViewModel<BluetoothViewModel>()
    val context = LocalContext.current
    val devices by viewModel.devices.observeAsState(emptyList())
    val coroutineScope = rememberCoroutineScope()

    val startDiscovery = rememberUpdatedState {
        viewModel.startDiscovery()
    }

    LaunchedEffect(Unit) {
        startDiscovery.value()
        delay(30000) // Wait for 30 seconds
        viewModel.stopDiscovery()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopDiscovery()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bluetooth Connections") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { startDiscovery.value() }) { // Start discovery again
                        Icon(Icons.Filled.Refresh, contentDescription = "Scan")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
                BluetoothDevicesScreen(
                    pairedDevices = devices,
                    nonPairedDevices = devices.filter { it.bondState != BluetoothDevice.BOND_BONDED },
                    onDeviceClick = { device ->
                        if (device.bondState != BluetoothDevice.BOND_BONDED) {
                            coroutineScope.launch {
                                Log.d("screendevices","$device")

                                viewModel.pairDevice(device) // Ensure 'pairDevice' is a function in BluetoothViewModel
                            }
                        } else {
                            coroutineScope.launch {

                                viewModel.connectDevice(device) // Ensure 'connectDevice' is a function in BluetoothViewModel
                            }
                        }
                    }
                )
            } else {
                Text("Bluetooth permissions are required.")
            }
        }

    }

}
