package com.example.rakaat.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val bluetoothReceiver: BluetoothReceiver
) : ViewModel() {

    val devices: LiveData<List<BluetoothDevice>> = bluetoothReceiver.devices
    private val _connectionStatus = MutableLiveData<Boolean>()
    val connectionStatus: LiveData<Boolean> = _connectionStatus
    private val _pairingStatus = MutableLiveData<Pair<BluetoothDevice, Boolean>>()
    val pairingStatus: LiveData<Pair<BluetoothDevice, Boolean>> = _pairingStatus

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun startDiscovery() {
        bluetoothReceiver.startDiscovery()
    }

    fun stopDiscovery() {
        bluetoothReceiver.stopDiscovery()
    }

    fun pairDevice(device: BluetoothDevice) {
        bluetoothReceiver.pairDevice(device) { isPaired ->
            _pairingStatus.postValue(Pair(device, isPaired))
        }
    }


    @SuppressLint("MissingPermission")
    fun connectDevice(device: BluetoothDevice) {
        viewModelScope.launch {
            try {
                bluetoothReceiver.connectSocket(device) { isConnected ->
                    _connectionStatus.postValue(isConnected)
                    if (!isConnected) {
                        _errorMessage.postValue("Failed to connect to ${device.name}")
                    }
                }
            } catch (e: Exception) {
                _errorMessage.postValue(e.message ?: "Error connecting to device")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopDiscovery()
        bluetoothReceiver.closeConnection()
    }


}