package com.ceyhan.weather.view

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.view.View
import android.widget.RemoteViews
import com.ceyhan.weather.BuildConfig
import com.ceyhan.weather.R
import com.ceyhan.weather.model.PlaceDetails
import com.ceyhan.weather.model.Weather
import com.ceyhan.weather.service.ApiService
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.Locale


class WeatherWidget : AppWidgetProvider() {
    private val disposable = CompositeDisposable()
    private val apiService = ApiService()
    private val jobs = ArrayList<Boolean>()

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.weather_widget)
        views.setViewVisibility(R.id.widgetProgressBar,View.VISIBLE)
        views.setViewVisibility(R.id.weatherWidgetLayout,View.INVISIBLE)
        views.setViewVisibility(R.id.widgetErrorText,View.INVISIBLE)
        views.setViewVisibility(R.id.widgetWarningEnterApp,View.INVISIBLE)

        getLocationData(context) { latitude, longitude ->
            if (latitude != null && longitude != null) {
                getWeatherFromApi(latitude.toDouble(),longitude.toDouble()) {weather ->
                    if (weather != null) {
                        views.run {
                            setImageViewResource(R.id.widgetBg,getBackgroundImg(weather.currentConditions.icon))
                            setImageViewResource(R.id.widgetIcon,getWeatherIcon(weather.currentConditions.icon))

                            setTextViewText(R.id.widgetTemp,"${weather.currentConditions.temp.toInt()}°")
                            setTextViewText(R.id.widgetFeelsLike,"${context.getText(R.string.feelslike)}: ${weather.currentConditions.feelslike.toInt()}°")

                            setTextViewText(R.id.widgetWindSpeed,"${context.getString(R.string.wind)}: ${weather.currentConditions.windspeed} ${context.getString(R.string.kph)}")
                            setTextViewText(R.id.widgetPrecipitationChance,"${context.getString(R.string.precip)}: %${weather.currentConditions.precipprob.toInt()}")
                            setTextViewText(R.id.widgetHumidity,"${context.getString(R.string.humidity)}: %${weather.currentConditions.humidity.toInt()}")

                            completeJob(views,appWidgetManager,appWidgetId,true)
                        }
                    }
                    else {
                        completeJob(views,appWidgetManager,appWidgetId,false)
                    }
                }
                getPlaceDetails(latitude.toDouble(),longitude.toDouble()) {placeDetails ->
                    if (placeDetails != null) {
                        views.run {
                            var city: String? = null
                            var district: String? = null
                            for (place in placeDetails.results[0].addressComponents) {
                                if (place.types.contains("administrative_area_level_1")) {
                                    city = place.longName
                                }
                                if (place.types.contains("administrative_area_level_2")) {
                                    district = place.longName
                                }
                            }
                            if (city != null && district != null) {
                                setTextViewText(R.id.widgetLocation,"$district, $city")
                            }
                            else {
                                setTextViewText(R.id.widgetLocation,context.getString(R.string.location_not_found))
                            }
                            completeJob(views,appWidgetManager,appWidgetId,true)
                        }
                    }
                    else {
                        completeJob(views,appWidgetManager,appWidgetId,false)
                    }
                }
            }
            else {
                views.setViewVisibility(R.id.widgetProgressBar,View.INVISIBLE)
                views.setViewVisibility(R.id.weatherWidgetLayout,View.INVISIBLE)
                views.setViewVisibility(R.id.widgetErrorText,View.INVISIBLE)
                views.setViewVisibility(R.id.widgetWarningEnterApp,View.VISIBLE)
            }
        }
    }

    private fun completeJob(views: RemoteViews, appWidgetManager: AppWidgetManager, appWidgetId: Int, jobStatus: Boolean) {
        jobs.add(jobStatus)
        if (jobs.size == 2) {
            views.setViewVisibility(R.id.widgetProgressBar,View.INVISIBLE)
            views.setViewVisibility(R.id.weatherWidgetLayout,View.INVISIBLE)
            views.setViewVisibility(R.id.widgetErrorText,View.INVISIBLE)
            views.setViewVisibility(R.id.widgetWarningEnterApp,View.INVISIBLE)
            if (jobs.all { it }) {
                views.setViewVisibility(R.id.weatherWidgetLayout,View.VISIBLE)
            }
            else {
                views.setViewVisibility(R.id.widgetErrorText,View.VISIBLE)
                //Error View
            }
            disposable.clear()
            jobs.clear()
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun getLocationData(context: Context, complete: (latitude: String?, longitude: String?) -> Unit) {
        val sharedPreferences = context.getSharedPreferences("Location",Context.MODE_PRIVATE)
        val latitude = sharedPreferences.getString("latitude",null)
        val longitude = sharedPreferences.getString("longitude",null)
        complete(latitude,longitude)
    }

    private fun getWeatherFromApi(latitude: Double, longitude: Double, complete: (weather: Weather?) -> Unit) {
        disposable.add(
            apiService.getWeather(latitude,longitude, Locale.getDefault().language, BuildConfig.WEATHER_API_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<Weather>() {
                    override fun onSuccess(t: Weather) {
                        complete(t)
                    }

                    override fun onError(e: Throwable) {
                        complete(null)
                    }

                })
        )
    }

    private fun getPlaceDetails(latitude: Double, longitude: Double, complete: (placeDetails: PlaceDetails?) -> Unit) {
        disposable.add(
            apiService.getPlaceDetails(latitude,longitude,BuildConfig.GOOGLE_MAPS_API_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<PlaceDetails>() {
                    override fun onSuccess(t: PlaceDetails) {
                        complete(t)
                    }

                    override fun onError(e: Throwable) {
                        complete(null)
                    }

                })
        )
    }

    private fun getBackgroundImg(iconName: String): Int {
        return when (iconName) {
            "snow" ->                   R.drawable.bg_snow_day
            "snow-showers-day" ->       R.drawable.bg_snow_day
            "snow-showers-night" ->     R.drawable.bg_snow_night
            "thunder-rain" ->           R.drawable.bg_rain_day
            "thunder-showers-day" ->    R.drawable.bg_rain_day
            "thunder-showers-night" ->  R.drawable.bg_rain_night
            "rain" ->                   R.drawable.bg_rain_day
            "showers-day" ->            R.drawable.bg_rain_day
            "showers-night" ->          R.drawable.bg_rain_night
            "fog" ->                    R.drawable.bg_fog
            "wind" ->                   R.drawable.bg_wind
            "cloudy" ->                 R.drawable.bg_cloudy_day
            "partly-cloudy-day" ->      R.drawable.bg_cloudy_day
            "partly-cloudy-night" ->    R.drawable.bg_cloudy_night
            "clear-day" ->              R.drawable.bg_clear_day
            "clear-night" ->            R.drawable.bg_clear_night
            else ->                     R.drawable.bg_snow
        }
    }

    private fun getWeatherIcon(iconName: String): Int {
        return when (iconName) {
            "snow" ->                   R.drawable.ic_snow
            "snow-showers-day" ->       R.drawable.ic_snow_day
            "snow-showers-night" ->     R.drawable.ic_snow_night
            "thunder-rain" ->           R.drawable.ic_thunder_rain
            "thunder-showers-day" ->    R.drawable.ic_thunder_rain_day
            "thunder-showers-night" ->  R.drawable.ic_thunder_rain_night
            "rain" ->                   R.drawable.ic_rain
            "showers-day" ->            R.drawable.ic_rain_day
            "showers-night" ->          R.drawable.ic_rain_night
            "fog" ->                    R.drawable.ic_fog
            "wind" ->                   R.drawable.ic_wind
            "cloudy" ->                 R.drawable.ic_couldy
            "partly-cloudy-day" ->      R.drawable.ic_couldy_day
            "partly-cloudy-night" ->    R.drawable.ic_couldy_night
            "clear-day" ->              R.drawable.ic_clear_day
            "clear-night" ->            R.drawable.ic_clear_night
            else ->                     R.drawable.weather
        }
    }
}



