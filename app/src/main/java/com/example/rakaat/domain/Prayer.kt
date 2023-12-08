package com.example.rakaat.domain

data class Prayer(val type: PrayerType, var rakatCount: Int = 0, var sajdaCount: Int = 0)
