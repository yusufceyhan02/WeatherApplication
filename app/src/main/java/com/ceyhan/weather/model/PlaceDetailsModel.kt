package com.ceyhan.weather.model

import com.google.gson.annotations.SerializedName

data class PlaceDetails (
    val results  : ArrayList<Results>,
    val status   : String?,
)

data class AddressComponents (
    @SerializedName("long_name") val longName  : String?,
    @SerializedName("short_name") val shortName : String?,
    @SerializedName("types") val types     : ArrayList<String>
)

data class Location (
    val lat : Double?,
    val lng : Double?
)

data class Geometry (
    val location: Location?
)


data class Results (
    @SerializedName("address_components") val addressComponents : ArrayList<AddressComponents>,
    @SerializedName("geometry")           val geometry          : Geometry?,
    @SerializedName("place_id")           val placeId           : String?
)