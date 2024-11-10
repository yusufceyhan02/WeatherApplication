package com.ceyhan.weather.service

import android.content.Context
import com.ceyhan.weather.model.PlaceDetails
import com.ceyhan.weather.model.Weather
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import io.reactivex.rxjava3.core.Single
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiService {
    private val weatherApi = Retrofit.Builder()
        .baseUrl("https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/")
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(Apis::class.java)

    private val placeApi = Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/maps/api/geocode/")
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(Apis::class.java)

    fun getWeather(latitude: Double, longitude: Double, language: String, weatherApiKey: String) : Single<Weather> {
        return weatherApi.getWeatherData(latitude.toString(),longitude.toString(),language,weatherApiKey)
    }

    fun getPlaceDetails(latitude: Double, longitude: Double, googleApiKey: String): Single<PlaceDetails> {
        return placeApi.getPlaceDetails("$latitude,$longitude",googleApiKey)
    }
}