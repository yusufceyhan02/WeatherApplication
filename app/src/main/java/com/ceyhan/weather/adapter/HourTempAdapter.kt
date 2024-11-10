package com.ceyhan.weather.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ceyhan.weather.databinding.RecyclerHourTempBinding
import com.ceyhan.weather.model.Hour

class HourTempAdapter(private val hours: ArrayList<Hour>): RecyclerView.Adapter<HourTempAdapter.HourTempHolder>() {
    class HourTempHolder(val binding: RecyclerHourTempBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourTempHolder {
        val recyclerHourTempBinding = RecyclerHourTempBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return HourTempHolder(recyclerHourTempBinding)
    }

    override fun getItemCount(): Int {
        return hours.size
    }

    override fun onBindViewHolder(holder: HourTempHolder, position: Int) {
        val hourText = hours[position].datetime.substring(0,5)
        val tempText = "${hours[position].temp.toInt()}°C"
        holder.binding.recyclerHour.text = hourText
        holder.binding.recyclerTemp.text = tempText
    }
}