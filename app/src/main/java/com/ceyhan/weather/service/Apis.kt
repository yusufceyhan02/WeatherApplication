package com.ceyhan.weather.service

import com.ceyhan.weather.model.PlaceDetails
import com.ceyhan.weather.model.Weather
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface Apis {
    @GET("{latitude},{longitude}?unitGroup=metric")
    fun getWeatherData(
        @Path("latitude") latitude: String,
        @Path("longitude") longitude: String,
        @Query("lang") language: String,
        @Query("key") weatherApiKey: String
    ): Single<Weather>

    @GET("json?location_type=ROOFTOP&result_type=street_address")
    fun getPlaceDetails(
        @Query("latlng") latLng: String,
        @Query("key") googleApiKey: String
    ): Single<PlaceDetails>
}