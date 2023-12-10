package com.example.rakaat.domain

import androidx.fragment.app.FragmentManager
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.*

fun getCurrentPrayer(prayerTimes: List<PrayerTime>): PrayerType {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val minute = Calendar.getInstance().get(Calendar.MINUTE)
    val currentTime = hour * 60 + minute

    val sortedPrayers = prayerTimes.sortedBy { it.time.split(":").let { it[0].toInt() * 60 + it[1].toInt() } }
    for (i in sortedPrayers.indices) {
        val startTime = sortedPrayers[i].time.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
        val endTime = if (i < sortedPrayers.size - 1) {
            sortedPrayers[i + 1].time.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
        } else {
            24 * 60 // End of day
        }

        if (currentTime in startTime until endTime) {
            return sortedPrayers[i].prayer
        }
    }

    return PrayerType.العشاء // Default if time doesn't match any period
}

fun showTimePicker(fragmentManager: FragmentManager, initialTime: String, onTimeSelected: (String) -> Unit) {
    val (hour, minute) = initialTime.split(":").map { it.toInt() }
    val picker = MaterialTimePicker.Builder()
        .setTimeFormat(TimeFormat.CLOCK_24H)
        .setHour(hour)
        .setMinute(minute)
        .build()

    picker.addOnPositiveButtonClickListener {
        val newTime = String.format("%02d:%02d", picker.hour, picker.minute)
        onTimeSelected(newTime)
    }

    picker.show(fragmentManager, "TimePicker")
}

