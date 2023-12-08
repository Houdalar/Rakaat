package com.example.rakaat.ui.screens

import android.app.Application
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rakaat.data.BluetoothReceiver
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application() {

}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var bluetoothReceiver: BluetoothReceiver
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the permission launcher
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // Check if all permissions are granted
            val allPermissionsGranted = permissions.entries.all { it.value }
            if (allPermissionsGranted) {
                // All permissions are granted
                bluetoothReceiver.startDiscovery() // Start Bluetooth discovery
                Toast.makeText(this, "Bluetooth permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                // Permissions are denied
                Toast.makeText(this, "Bluetooth permissions denied", Toast.LENGTH_SHORT).show()
            }
        }

        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "PrayerCounterScreen") {
                composable("PrayerCounterScreen") {
                    PrayerCounterScreen(navController)
                }
                composable("bluetoothConnectionScreen") {
                    BluetoothConnectionScreen(
                        requestPermissionLauncher = requestPermissionLauncher,
                        navController = navController,
                        onConnected = {
                            Toast.makeText(this@MainActivity, "Device connected", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}