package com.example.rakaat.ui.screens

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rakaat.data.BluetoothManager
import com.example.rakaat.data.BluetoothReceiver
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltAndroidApp
class MyApplication : Application() {

}

@AndroidEntryPoint
class MainActivity : AppCompatActivity(){
    @Inject
    lateinit var bluetoothReceiver: BluetoothReceiver

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the permission launcher
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allPermissionsGranted = permissions.entries.all { it.value }
            if (allPermissionsGranted) {
                Toast.makeText(this, "Bluetooth permissions granted", Toast.LENGTH_SHORT).show()
                attemptAutoConnect()
            } else {
                requestBluetoothPermissions()
            }
        }


        checkAndAutoConnect()

        setContent {
            val navController = rememberNavController()
            val fragmentManager = supportFragmentManager



            NavHost(navController = navController, startDestination = "PrayerCounterScreen") {
                composable("PrayerCounterScreen") {
                    PrayerCounterScreen(navController)
                }
                composable("bluetoothConnectionScreen") {
                    BluetoothConnectionScreen(
                        requestPermissionLauncher = requestPermissionLauncher,
                        navController = navController,
                        onConnected = {}
                    )
                }
                composable("settingsScreen") {
                    SettingsScreen(navController)
                }
                composable("PrayerTimeSettingsScreen") {
                    PrayerTimeSettingsScreen(navController, fragmentManager)
                }
            }
        }
    }

    private fun checkAndAutoConnect() {
        if (BluetoothAdapter.getDefaultAdapter()?.isEnabled == true &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
            attemptAutoConnect()
        } else {
            requestBluetoothPermissions()
        }
    }

    private fun requestBluetoothPermissions() {
        requestPermissionLauncher.launch(arrayOf(
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN
        ))
    }

    private fun attemptAutoConnect() {
        CoroutineScope(Dispatchers.Main).launch {
            BluetoothManager.autoConnectToDevice(this@MainActivity)
        }
    }
}