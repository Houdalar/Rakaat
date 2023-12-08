package com.example.rakaat.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rakaat.domain.Prayer
import com.example.rakaat.domain.getCurrentPrayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrayerViewModel @Inject constructor(private val bluetoothReceiver: BluetoothReceiver) : ViewModel() {
    val _currentPrayer = MutableLiveData<Prayer>(Prayer(getCurrentPrayer()))
    val currentPrayer: LiveData<Prayer> = _currentPrayer

    init {
        listenForBluetoothSignals()
    }

    private fun listenForBluetoothSignals() {
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                val signal = bluetoothReceiver?.listenForSignal()
                when (signal) {
                    'A' -> incrementRakat()
                    'B' -> incrementSajda()
                }
            }
        }
    }

    private fun incrementRakat() {
        val prayer = _currentPrayer.value
        prayer?.let {
            if (it.rakatCount < it.type.rakatCount) {
                it.rakatCount++
                _currentPrayer.postValue(it)
            }
        }
    }

    private fun incrementSajda() {
        val prayer = _currentPrayer.value
        prayer?.let {
            it.sajdaCount ++ // Two sajda for each rakat
            _currentPrayer.postValue(it)
        }
    }
}

