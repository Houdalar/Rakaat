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
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID

object BluetoothManager {
    private var bluetoothSocket: BluetoothSocket? = null
    private val myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val _devices = MutableLiveData<List<BluetoothDevice>>()
    val devices: LiveData<List<BluetoothDevice>> get() = _devices
    private var isReceiverRegistered = false
    private lateinit var receiver: BroadcastReceiver
    var currentlyConnectedDeviceAddress: String? = null
        private set


    init {
        initializeReceiver()
    }

    private fun initializeReceiver() {
        receiver = object : BroadcastReceiver() {
            @SuppressLint("MissingPermission")
            override fun onReceive(context: Context, intent: Intent) {
                val action: String = intent.action!!
                if (BluetoothDevice.ACTION_FOUND == action) {
                    val discoveredDevice: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    Log.d("BluetoothManager", "Device found: ${discoveredDevice.name}")

                    // Temporary set to avoid duplicates
                    val currentDevicesSet = _devices.value.orEmpty().toMutableSet()
                    if (currentDevicesSet.add(discoveredDevice)) {
                        _devices.value = currentDevicesSet.toList()
                    }
                }
            }
        }
    }


    fun startDiscovery(context: Context) {
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

    fun stopDiscovery(context: Context) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
            if (isReceiverRegistered) {
                context.unregisterReceiver(receiver)
                isReceiverRegistered = false
            }
            bluetoothAdapter.cancelDiscovery()
        }
    }

    fun pairDevice(context: Context,device: BluetoothDevice, onPairingResult: (Boolean) -> Unit) {
        val pairingRequestReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action: String = intent.action!!
                if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == action && ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
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
    suspend fun connectSocket(device: BluetoothDevice, context: Context, onConnectionResult: (Boolean) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                if (bluetoothAdapter.isEnabled && device.bondState == BluetoothDevice.BOND_BONDED) {
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID)

                    if (bluetoothSocket == null) {
                        Log.e("BluetoothReceiver", "Failed to create Bluetooth socket")
                        onConnectionResult(false)
                        return@withContext
                    }

                    Log.d("BluetoothReceiver", "Attempting to connect to Bluetooth device")
                    bluetoothSocket?.connect()

                    if (bluetoothSocket?.isConnected == true) {
                        saveConnectedDeviceAddress(context, device.address)
                        currentlyConnectedDeviceAddress = device.address

                        onConnectionResult(true)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Connected to ${device.name}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        onConnectionResult(false)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "failed to Connect to ${device.name}", Toast.LENGTH_SHORT).show()
                        }
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
    private fun saveConnectedDeviceAddress(context: Context, address: String) {
        val sharedPreferences = context.getSharedPreferences("BluetoothPreferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("LastConnectedDevice", address).apply()
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
                Log.d("BluetoothReceiver", "received : $firstChar")
                return firstChar
            } catch (e: IOException) {
                Log.e("BluetoothReceiver", "Error reading Bluetooth signal", e)
                return ' ' // or some other error indicator
            }
        } else {
          //  Log.e("BluetoothReceiver", "BluetoothSocket is not connected or InputStream is null")
            return ' ' // or some other error indicator
        }
    }
    suspend fun autoConnectToDevice(context: Context) {
        val sharedPreferences = context.getSharedPreferences("BluetoothPreferences", Context.MODE_PRIVATE)
        val address = sharedPreferences.getString("LastConnectedDevice", null)
Log.d("bleutoothManager","$address")
        address?.let {
            val device = bluetoothAdapter.getRemoteDevice(address)
            connectSocket(device, context) { isConnected ->
                if (isConnected) {
                    // Handle successful connection
                } else {
                    // Handle failed connection
                }
            }
        }
    }
    open fun closeConnection() {
        try {
            bluetoothSocket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun disconnectCurrentDevice(context: Context) {
        if (bluetoothSocket?.isConnected == true) {
            closeConnection()
            currentlyConnectedDeviceAddress = null
            Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show()
        }
    }
}