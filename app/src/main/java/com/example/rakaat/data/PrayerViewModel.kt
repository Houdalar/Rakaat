package com.example.rakaat.data

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.constraintlayout.solver.state.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rakaat.domain.Prayer
import com.example.rakaat.domain.PrayerTime
import com.example.rakaat.domain.PrayerType
import com.example.rakaat.domain.getCurrentPrayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class PrayerViewModel @Inject constructor(private val bluetoothReceiver: BluetoothReceiver,    private val application: Application // Inject Application
) : ViewModel() {
    private val _prayerTimes = MutableStateFlow(loadPrayerTimes())
    private val _currentPrayer = MutableStateFlow(Prayer(getCurrentPrayer(_prayerTimes.value)))
    val currentPrayer: StateFlow<Prayer> = _currentPrayer.asStateFlow()
    private val _updateTrigger = mutableStateOf(0)
    val updateTrigger = _updateTrigger
    init {
        viewModelScope.launch {
            _prayerTimes.collect { times ->
                _currentPrayer.value = Prayer(getCurrentPrayer(times))
            }
        }
        listenForBluetoothSignals()
    }

    private fun listenForBluetoothSignals() {
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                val signal = BluetoothManager?.listenForSignal()
                when (signal) {
                    'A' -> incrementSajda()

                }
            }
        }
    }

    val prayerTimes: StateFlow<List<PrayerTime>> = _prayerTimes.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun updatePrayerTime(prayer: PrayerType, newTime: String, context: Context) {
        // Update the prayer times in StateFlow
        val updatedTimes = _prayerTimes.value.map {
            if (it.prayer == prayer) it.copy(time = newTime) else it
        }
        _prayerTimes.value = updatedTimes

        // Save the updated times to Shared Preferences
        val sharedPrefs = application.getSharedPreferences("PrayerTimes", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            updatedTimes.forEach {
                putString(it.prayer.name, it.time)
            }
            apply()
        }

        // Force refresh of current prayer
        viewModelScope.launch {
            _currentPrayer.emit(Prayer(getCurrentPrayer(updatedTimes)))
        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateCurrentPrayerBasedOnUpdatedTimes(): Prayer {
        val currentTime = LocalTime.now()
        val sortedPrayers = _prayerTimes.value.sortedBy { LocalTime.parse(it.time) }

        for (prayer in sortedPrayers) {
            val prayerTime = LocalTime.parse(prayer.time)
            if (currentTime.isBefore(prayerTime)) {
                return Prayer(prayer.prayer)
            }
        }

        // If current time is after all prayer times, return the last prayer of the day
        return Prayer(sortedPrayers.last().prayer)
    }

    private fun incrementSajda() {
        val prayer = _currentPrayer.value
        val newSajdaCount = prayer.sajdaCount + 1

        if (newSajdaCount >= 2) {
            // If 2 sajdas are reached, increment rakat and reset sajda count
            val newRakatCount = if (prayer.rakatCount < prayer.type.rakatCount) prayer.rakatCount + 1 else prayer.rakatCount
            _currentPrayer.value = prayer.copy(rakatCount = newRakatCount, sajdaCount = 0)
        } else {
            // Otherwise, just increment sajda count
            _currentPrayer.value = prayer.copy(sajdaCount = newSajdaCount)
        }
    }
    private fun loadPrayerTimes(): List<PrayerTime> {
        val sharedPrefs = application.getSharedPreferences("PrayerTimes", Context.MODE_PRIVATE)
        return PrayerType.values().map { prayerType ->
            val defaultTime = getDefaultTimeForPrayer(prayerType)
            val time = sharedPrefs.getString(prayerType.name, defaultTime) ?: defaultTime
            PrayerTime(prayerType, time)
        }
    }


    fun getDefaultTimeForPrayer(prayer: PrayerType): String {
        // Return default time string based on the prayer type
        return when (prayer) {
            PrayerType.الصبح -> "05:00"
            PrayerType.الظهر -> "12:00"
            PrayerType.العصر -> "15:00"
            PrayerType.المغرب -> "17:00"
            PrayerType.العشاء -> "19:00"

            else -> {"20:00"}
        }
}
}

