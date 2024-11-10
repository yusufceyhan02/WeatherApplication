package com.ceyhan.weather.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ceyhan.weather.R
import com.ceyhan.weather.databinding.BottomSheetDayWeatherBinding
import com.ceyhan.weather.databinding.RecyclerWeatherDayBinding
import com.ceyhan.weather.model.WeatherDayModel
import com.google.android.material.bottomsheet.BottomSheetDialog

class WeatherDayRecyclerAdapter(private val weatherDays: ArrayList<WeatherDayModel>, val context: Context): RecyclerView.Adapter<WeatherDayRecyclerAdapter.WeatherDayRecyclerHolder>() {
    private lateinit var bottomSheet: BottomSheetDialog
    private lateinit var bottomSheetBinding: BottomSheetDayWeatherBinding

    class WeatherDayRecyclerHolder(val binding: RecyclerWeatherDayBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherDayRecyclerHolder {
        bottomSheet = BottomSheetDialog(context)
        bottomSheetBinding = BottomSheetDayWeatherBinding.inflate(LayoutInflater.from(context),null,false)
        bottomSheet.setContentView(bottomSheetBinding.root)

        val recyclerWeatherDayBinding = RecyclerWeatherDayBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return WeatherDayRecyclerHolder(recyclerWeatherDayBinding)
    }

    override fun getItemCount(): Int {
        return weatherDays.size - 1
    }

    override fun onBindViewHolder(holder: WeatherDayRecyclerHolder, position: Int) {
        val weather = weatherDays[position+1]

        holder.binding.watDayName.text = weather.dayName
        holder.binding.watDayIcon.setImageResource(weather.dayIcon)
        val tempText = "${weather.temp.toInt()}°C"
        holder.binding.watDayTemp.text = tempText

        holder.itemView.setOnClickListener {

            bottomSheetBinding.run {
                BottomSheetBg.setImageResource(weather.weatherBg)

                BottomSheetDate.text = weather.datetime

                BottomSheetTemp.text = weather.temp.toInt().toString()

                val detailsTempMaxText = "${weather.tempMax}°C"
                BottomSheetDetailsTempMax.text = detailsTempMaxText

                val detailsTempMinText = "${weather.tempMin}°C"
                BottomSheetDetailsTempMin.text = detailsTempMinText

                val feelsText = "${context.getString(R.string.feelslike)}: ${weather.feelslike.toInt()}°"
                BottomSheetFeels.text = feelsText

                val windSpeedText = "${context.getString(R.string.wind)}: ${weather.windSpeed} ${context.getString(R.string.kph)}"
                BottomSheetWindSpeed.text = windSpeedText

                val precipitationChance = "${context.getString(R.string.precip)}: %${weather.precipProb.toInt()}"
                BottomSheetPrecipitationChance.text = precipitationChance

                val humidity = "${context.getString(R.string.humidity)}: %${weather.humidity.toInt()}"
                BottomSheetHumidity.text = humidity

                val detailsDewText = "${weather.dew}°C"
                BottomSheetDetailsDew.text = detailsDewText

                BottomSheetIcon.setImageResource(weather.dayIcon)

                val humidityText = "%${weather.humidity.toInt()}"
                BottomSheetDetailsHumidity.text = humidityText

                val precipProbText = "%${weather.precipProb.toInt()}"
                BottomSheetDetailsPrecipProb.text = precipProbText

                val detailsWindSpeedText = "${weather.windSpeed} ${context.getString(R.string.kph)}"
                BottomSheetDetailsWindSpeed.text = detailsWindSpeedText

                val detailsUvIndexText = "${weather.uvIndex.toInt()}"
                BottomSheetDetailsUvIndex.text = detailsUvIndexText

                val detailsPressureText = "${weather.pressure.toInt()} mb"
                BottomSheetDetailsPressure.text = detailsPressureText

                BottomSheetDetailsMoonPhaseIcon.setImageResource(weather.moonPhase)

                BottomSheetHourTempRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
                BottomSheetHourTempRecyclerView.adapter = HourTempAdapter(ArrayList(weather.hours))
            }
            bottomSheet.show()
        }
    }
}