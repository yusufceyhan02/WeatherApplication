package com.ceyhan.weather.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ceyhan.weather.BuildConfig
import com.ceyhan.weather.model.PlaceDetails
import com.ceyhan.weather.model.Weather
import com.ceyhan.weather.service.ApiService
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.Locale

class WeatherViewModel : ViewModel() {
    val weatherData = MutableLiveData<Weather>()
    val placeDetails = MutableLiveData<PlaceDetails>()
    val progress = MutableLiveData<Boolean>()
    val error = MutableLiveData<Boolean>()

    private val jobs = ArrayList<Boolean>()
    private val disposable = CompositeDisposable()
    private val apiService = ApiService()

    fun getLocationData(latitude: Double, longitude: Double) {
        progress.value = true
        getPlaceDetails(latitude, longitude)
        getWeatherFromApi(latitude, longitude)
    }

    private fun getWeatherFromApi(latitude: Double, longitude: Double) {
        disposable.add(
            apiService.getWeather(latitude,longitude,Locale.getDefault().language,BuildConfig.WEATHER_API_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<Weather>() {
                    override fun onSuccess(t: Weather) {
                        weatherData.value = t
                        completeJob(true)
                    }

                    override fun onError(e: Throwable) {
                        println(e.message)
                        completeJob(false)
                    }

                })
        )
    }

    private fun getPlaceDetails(latitude: Double, longitude: Double) {
        disposable.add(
            apiService.getPlaceDetails(latitude,longitude,BuildConfig.GOOGLE_MAPS_API_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<PlaceDetails>() {
                    override fun onSuccess(t: PlaceDetails) {
                        placeDetails.value = t
                        completeJob(true)
                    }

                    override fun onError(e: Throwable) {
                        completeJob(false)
                    }

                })
        )
    }

    private fun completeJob(jobStatus: Boolean) {
        jobs.add(jobStatus)
        if (jobs.size == 2) {
            progress.value = false
            error.value = !jobs.all { true }
            disposable.clear()
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}