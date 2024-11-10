package com.ceyhan.weather.view

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.ceyhan.weather.R
import com.ceyhan.weather.adapter.HourTempAdapter
import com.ceyhan.weather.adapter.WeatherDayRecyclerAdapter
import com.ceyhan.weather.databinding.FragmentWeatherBinding
import com.ceyhan.weather.model.WeatherDayModel
import com.ceyhan.weather.viewModel.WeatherViewModel
import com.google.android.gms.maps.model.LatLng
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WeatherFragment : Fragment() {
    private lateinit var binding: FragmentWeatherBinding
    private lateinit var viewModel: WeatherViewModel
    private lateinit var location: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[WeatherViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentWeatherBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getArgument {
            onClicks()
            setObservers()
            viewModel.getLocationData(location.latitude,location.longitude)
            setDataForView()
        }
    }

    private fun onClicks() {
        binding.changeLocationButton.setOnClickListener {
            Navigation.findNavController(binding.root).navigate(R.id.action_weatherFragment_to_selectLocationFragment)
        }
        binding.weatherErrorTryAgainButton.setOnClickListener {
            viewModel.getLocationData(location.latitude,location.longitude)
        }
    }

    private fun getArgument(complete: () -> Unit) {
        val sharedPreferences = requireActivity().getSharedPreferences("Location",Context.MODE_PRIVATE)
        val latitude = sharedPreferences.getString("latitude",null)
        val longitude = sharedPreferences.getString("longitude",null)

        if (latitude != null && longitude != null){
            location = LatLng(latitude.toDouble(),longitude.toDouble())
            complete()
        }
        else {
            Navigation.findNavController(binding.root).navigate(R.id.action_weatherFragment_to_selectLocationFragment)
        }
    }

    private fun setObservers() {
        //Place Name
        viewModel.placeDetails.observe(viewLifecycleOwner) {placeDetails ->
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
                val locText = "$district, $city"
                binding.watLocation.text = locText
            }
            else {
                binding.watLocation.text = getString(R.string.location_not_found)
            }
        }

        //Weather Details
        viewModel.weatherData.observe(viewLifecycleOwner) {weather ->
            binding.weatherBg.setImageResource(getBackgroundImg(weather.currentConditions.icon))

            binding.watTemp.text = weather.currentConditions.temp.toInt().toString()

            val feelsText = "${getString(R.string.feelslike)}: ${weather.currentConditions.feelslike.toInt()}°"
            binding.watFeels.text = feelsText

            val windSpeedText = "${getString(R.string.wind)}: ${weather.currentConditions.windspeed} ${getString(R.string.kph)}"
            binding.watWindSpeed.text = windSpeedText

            val precipitationChance = "${getString(R.string.precip)}: %${weather.currentConditions.precipprob.toInt()}"
            binding.watPrecipitationChance.text = precipitationChance

            val humidity = "${getString(R.string.humidity)}: %${weather.currentConditions.humidity.toInt()}"
            binding.watHumidity.text = humidity

            binding.watIcon.setImageResource(getWeatherIcon(weather.currentConditions.icon))

            val detailsTempMaxText = "${weather.days[0].tempmax}°C"
            binding.watDetailsTempMax.text = detailsTempMaxText

            val detailsTempMinText = "${weather.days[0].tempmin}°C"
            binding.watDetailsTempMin.text = detailsTempMinText

            val humidityText = "%${weather.currentConditions.humidity.toInt()}"
            binding.watDetailsHumidity.text = humidityText

            val precipProbText = "%${weather.currentConditions.precipprob.toInt()}"
            binding.watDetailsPrecipProb.text = precipProbText

            val detailsWindSpeedText = "${weather.currentConditions.windspeed} ${getString(R.string.kph)}"
            binding.watDetailsWindSpeed.text = detailsWindSpeedText

            val detailsDewText = "${weather.currentConditions.dew}°C"
            binding.watDetailsDew.text = detailsDewText

            val detailsUvIndexText = "${weather.currentConditions.uvindex.toInt()}"
            binding.watDetailsUvIndex.text = detailsUvIndexText

            val detailsPressureText = "${weather.currentConditions.pressure.toInt()} mb"
            binding.watDetailsPressure.text = detailsPressureText

            binding.watDetailsMoonPhaseIcon.setImageResource(getMoonPhaseIcon(weather.currentConditions.moonphase))

            binding.hourTempRecyclerView.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
            binding.hourTempRecyclerView.adapter = HourTempAdapter(ArrayList(weather.days[0].hours))

            val weatherDays = ArrayList<WeatherDayModel>()
            for (weatherDay in weather.days) {
                weatherDays.add(WeatherDayModel(
                    LocalDate.parse(weatherDay.datetime).format(DateTimeFormatter.ofPattern("dd MMMM yyyy, EEEE")),
                    LocalDate.parse(weatherDay.datetime).format(DateTimeFormatter.ofPattern("EEE")),
                    getWeatherIcon(weatherDay.icon),
                    getBackgroundImg(weatherDay.icon),
                    weatherDay.tempmax,
                    weatherDay.tempmin,
                    weatherDay.temp,
                    weatherDay.feelslike,
                    weatherDay.windspeed,
                    weatherDay.precipprob,
                    weatherDay.humidity,
                    weatherDay.dew,
                    weatherDay.uvindex,
                    weatherDay.pressure,
                    getMoonPhaseIcon(weatherDay.moonphase),
                    weatherDay.hours
                ))
            }
            binding.weatherDayRecyclerView.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
            binding.weatherDayRecyclerView.adapter = WeatherDayRecyclerAdapter(weatherDays,requireContext())

        }

        //Progress Bar
        viewModel.progress.observe(viewLifecycleOwner) {progress ->
            if (progress) {
                binding.weatherBg.visibility = View.INVISIBLE
                binding.weatherBgMask.visibility = View.INVISIBLE
                binding.weatherView.visibility = View.INVISIBLE
                binding.weatherErrorLayout.visibility = View.INVISIBLE
                binding.weatherProgressBar.visibility = View.VISIBLE
            }
        }

        //Error
        viewModel.error.observe(viewLifecycleOwner) {error ->
            if (error){
                binding.weatherProgressBar.visibility = View.INVISIBLE
                binding.weatherView.visibility = View.INVISIBLE
                binding.weatherBg.visibility = View.INVISIBLE
                binding.weatherBgMask.visibility = View.INVISIBLE
                binding.weatherErrorLayout.visibility = View.VISIBLE
            }
            else {
                binding.weatherProgressBar.visibility = View.INVISIBLE
                binding.weatherErrorLayout.visibility = View.INVISIBLE
                binding.weatherBg.visibility = View.VISIBLE
                binding.weatherBgMask.visibility = View.VISIBLE
                binding.weatherView.visibility = View.VISIBLE
            }
        }
    }

    private fun setDataForView() {
        //date
        val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy, EEEE"))
        binding.watDate.text = date
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

    private fun getMoonPhaseIcon(moonPhase: Double): Int {
        val m = moonPhase.toFloat()
        return when {
            m == 0f ->                  R.drawable.new_moon
            m > 0f && m < 0.25f ->      R.drawable.waxing_crescent
            m == 0.25f ->               R.drawable.first_quarter
            m > 0.25f && m < 0.5f ->    R.drawable.waxing_gibbous
            m == 0.5f ->                R.drawable.full_moon
            m > 0.5f && m < 0.75f ->    R.drawable.waning_moon
            m == 0.75f ->               R.drawable.last_quarter
            m > 0.75f && m < 1f ->      R.drawable.waning_crescent
            else ->                     R.drawable.new_moon
        }
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
}