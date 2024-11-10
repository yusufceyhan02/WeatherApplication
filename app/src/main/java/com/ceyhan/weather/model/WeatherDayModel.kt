package com.ceyhan.weather.model

data class WeatherDayModel(
    val datetime: String,
    val dayName: String,
    val dayIcon: Int,
    val weatherBg: Int,
    val tempMax: Double,
    val tempMin: Double,
    val temp: Double,
    val feelslike: Double,
    val windSpeed: Double,
    val precipProb: Double,
    val humidity: Double,
    val dew: Double,
    val uvIndex: Double,
    val pressure: Double,
    val moonPhase: Int,
    val hours: List<Hour>
)
