package com.ceyhan.weather.view

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.ceyhan.weather.BuildConfig
import com.ceyhan.weather.R
import com.ceyhan.weather.databinding.FragmentSelectLocationBinding
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

class SelectLocationFragment : Fragment() {
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var locationPermission: ActivityResultLauncher<String>
    private lateinit var progressAlertDialog: Dialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSelectLocationBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createProgressDialog()
        registerLauncher()
        registerPlaces()
        onClicks()
    }

    //Google places api
    private fun registerPlaces() {
        Places.initialize(requireContext().applicationContext, BuildConfig.GOOGLE_MAPS_API_KEY)
        val autoCompleteFragment = childFragmentManager.findFragmentById(R.id.googleAutoCompleteFragment) as AutocompleteSupportFragment
        autoCompleteFragment.apply {
            setPlaceFields(listOf(Place.Field.LOCATION))
            setHint(getString(R.string.select_location))
            setOnPlaceSelectedListener(object : PlaceSelectionListener {
                override fun onPlaceSelected(p0: Place) {
                    locationOk(p0.location!!.latitude,p0.location!!.longitude)
                }

                override fun onError(p0: Status) {
                    Toast.makeText(requireContext(),getString(R.string.error_txt),Toast.LENGTH_LONG).show()
                }

            })
        }
    }

    private fun onClicks() {
        binding.useLocationButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            }
            else {
                locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun registerLauncher() {
        locationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {result ->
            if (result) {
                getLocation()
            }
        }
    }

    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            progress(true)
            val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,10000,1000f) {location ->
                locationOk(location.latitude,location.longitude)
            }
        }
        else {
            locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun locationOk(latitude: Double, longitude: Double) {
        val sharedPreferences = requireActivity().getSharedPreferences("Location",Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("latitude",latitude.toString()).apply()
        sharedPreferences.edit().putString("longitude",longitude.toString()).apply()
        progress(false)
        Navigation.findNavController(binding.root).navigate(R.id.action_selectLocationFragment_to_weatherFragment)
    }

    private fun progress(progress: Boolean) {
        binding.useLocationButton.isEnabled = progress
        if (progress) {
            progressAlertDialog.show()
        }
        else {
            progressAlertDialog.dismiss()
        }
    }

    private fun createProgressDialog() {
        progressAlertDialog = Dialog(requireContext())
        progressAlertDialog.setContentView(ProgressBar(requireContext()))
        progressAlertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        progressAlertDialog.setCanceledOnTouchOutside(false)
    }
}