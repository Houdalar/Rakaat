package com.example.rakaat.domain

import java.util.*

fun getCurrentPrayer(): PrayerType {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour in 0..8 -> PrayerType.الصبح
        hour in 9..14 -> PrayerType.الظهر
        hour in 15..16 -> PrayerType.العصر
        hour in 17..19 -> PrayerType.المغرب
        else -> PrayerType.العشاء
    }
}

