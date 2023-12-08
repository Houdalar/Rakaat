package com.example.rakaat.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*
import javax.inject.Inject


open class BluetoothReceiver @Inject constructor (private val context: Context, private val bluetoothAdapter: BluetoothAdapter) {
    private var bluetoothSocket: BluetoothSocket? = null
    private val myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private lateinit var device: BluetoothDevice
    private val _devices = MutableLiveData<List<BluetoothDevice>>()
    val devices: LiveData<List<BluetoothDevice>> = _devices
    private var isReceiverRegistered = false


    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String = intent.action!!
            if (BluetoothDevice.ACTION_FOUND == action) {
                val discoveredDevice: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!

                // Temporary set to avoid duplicates
                val currentDevicesSet = _devices.value.orEmpty().toMutableSet()
                if (currentDevicesSet.add(discoveredDevice)) {
                    _devices.value = currentDevicesSet.toList()
                }
            }
        }
    }

    fun startDiscovery() {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
            _devices.value = bluetoothAdapter.bondedDevices.toList()
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            if (!isReceiverRegistered) {
                context.registerReceiver(receiver, filter)
                isReceiverRegistered = true
            }
            bluetoothAdapter.startDiscovery()
        } else {
            // Handle the lack of permissions
        }
    }

    fun stopDiscovery() {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
            if (isReceiverRegistered) {
                context.unregisterReceiver(receiver)
                isReceiverRegistered = false
            }
            bluetoothAdapter.cancelDiscovery()
        }
    }

    fun pairDevice(device: BluetoothDevice, onPairingResult: (Boolean) -> Unit) {
        val pairingRequestReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action: String = intent.action!!
                if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == action &&ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
                    val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                    if (state == BluetoothDevice.BOND_BONDED) {
                        // Bonded successfully
                        onPairingResult(true)
                        context.unregisterReceiver(this)
                    } else if (state == BluetoothDevice.BOND_NONE) {
                        // Failed to bond
                        onPairingResult(false)
                        context.unregisterReceiver(this)
                    }
                }
            }
        }

        // Register the BroadcastReceiver
        context.registerReceiver(pairingRequestReceiver, IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED))

        // Initiate pairing
        device.createBond()
    }


    @SuppressLint("MissingPermission")
    fun connectSocket(device: BluetoothDevice, onConnectionResult: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (bluetoothAdapter.isEnabled && device.bondState == BluetoothDevice.BOND_BONDED) {
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID)

                    if (bluetoothSocket == null) {
                        Log.e("BluetoothReceiver", "Failed to create Bluetooth socket")
                        onConnectionResult(false)
                        return@launch
                    }

                    // Attempt to connect to the device's socket
                    Log.d("BluetoothReceiver", "Attempting to connect to Bluetooth device")
                    bluetoothSocket?.connect()

                    if (bluetoothSocket?.isConnected == true) {
                        Log.d("BluetoothReceiver", "Connected successfully")
                        onConnectionResult(true)
                    } else {
                        Log.e("BluetoothReceiver", "Failed to connect: Socket is not connected")
                        onConnectionResult(false)
                    }
                } else {
                    Log.e("BluetoothReceiver", "Bluetooth is not enabled or device is not bonded")
                    onConnectionResult(false)
                }
            } catch (e: IOException) {
                Log.e("BluetoothReceiver", "Connection failed: ${e.message}", e)
                bluetoothSocket?.close()
                Log.e("BluetoothReceiver", "Socket closed after failed connection")
                onConnectionResult(false)
            }
        }
    }


    open fun listenForSignal(): Char {
        val inputStream = bluetoothSocket?.inputStream
        if (inputStream != null && bluetoothSocket?.isConnected == true) {
            try {
                val buffer = ByteArray(1024)
                val bytes = inputStream.read(buffer)
                if (bytes == -1) {
                    // The socket might be closed. Handle this situation gracefully.
                    Log.e("BluetoothReceiver", "Socket appears to be closed. Reading returned -1.")
                    return ' ' // or some other error indicator
                }
                val readMessage = String(buffer, 0, bytes)
                val firstChar = readMessage.firstOrNull() ?: ' '
                return firstChar
            } catch (e: IOException) {
                Log.e("BluetoothReceiver", "Error reading Bluetooth signal", e)
                return ' ' // or some other error indicator
            }
        } else {
            Log.e("BluetoothReceiver", "BluetoothSocket is not connected or InputStream is null")
            return ' ' // or some other error indicator
        }
    }

    open fun closeConnection() {
        try {
            bluetoothSocket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

