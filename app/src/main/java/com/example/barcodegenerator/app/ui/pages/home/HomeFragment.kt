package com.example.barcodegenerator.app.ui.pages.home


import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.example.barcodegenerator.R
import com.example.barcodegenerator.app.ui.base.BaseFragment
import com.example.barcodegenerator.databinding.FragmentHomeBinding

/**
 * Created by AralBenli on 12.06.2023.
 */
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    private lateinit var locationManager: LocationManager
    private var location: Location? = null
    private val locationListener = MyLocationListener()
    private var latitude: String? = null
    private var longitude: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        Log.d("HomeFragment", "locationManager is initialized: ${::locationManager.isInitialized}")

    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    private lateinit var data: String
    override fun getViewBinding(): FragmentHomeBinding = FragmentHomeBinding.inflate(layoutInflater)

    override fun initViews() {
        binding.withoutLocation.isChecked
        setData()
        settingLocation()
    }

    private fun settingLocation() {
        with(binding) {
            // Initialize the radio group with the default checked button
            radioGroup.check(R.id.withoutLocation)

            // Set the checked change listener for the radio group
            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.withLocation -> {
                        // Check for location permission
                        if (ActivityCompat.checkSelfPermission(
                                requireContext(),
                                android.Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            // Location permission granted
                            binding.withLocation.isChecked = true
                            binding.withoutLocation.isChecked = false
                            getLocation()
                        } else {
                            // Request location permission
                            ActivityCompat.requestPermissions(
                                requireActivity(),
                                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                                LOCATION_PERMISSION_REQUEST_CODE
                            )
                            binding.withLocation.isChecked = false
                            binding.withoutLocation.isChecked = true
                            radioGroup.check(R.id.withoutLocation)
                        }
                    }
                    R.id.withoutLocation -> {
                        binding.withLocation.isChecked = false
                        binding.withoutLocation.isChecked = true
                    }
                }
            }
        }
    }

    private fun setData() {
        with(binding) {
            createButton.setOnClickListener {
                if (!edtMessage.text.toString().isNullOrEmpty()) {
                    data = edtMessage.text.toString()
                    val latitude = location?.latitude?.toString()
                    val longitude = location?.longitude?.toString()

                    Log.d("setData", "withLocation.isChecked: ${withLocation.isChecked}")
                    Log.d("setData", "Latitude: $latitude, Longitude: $longitude")

                    if (withLocation.isChecked) {
                        if (latitude != null && longitude != null) {
                            navigateToNextFragment(data, latitude, longitude)
                        } else {
                            showCustomToast(
                                Status.Fail,
                                "GPS is not available! Try again",
                                this@HomeFragment
                            )
                        }
                    } else if (withoutLocation.isChecked) {
                        navigateToNextFragment(data, "", "")
                    }
                    edtMessage.text.clear()
                } else {
                    showCustomToast(
                        Status.Warn,
                        "Information space can't be empty",
                        this@HomeFragment
                    )
                }
            }
        }
    }

    private fun navigateToNextFragment(data: String, latitude: String?, longitude: String?) {
        if (binding.withLocation.isChecked) {
            if (!latitude.isNullOrEmpty() && !longitude.isNullOrEmpty()) {
                val bundle = bundleOf(
                    "edtData" to data,
                    "latitude" to latitude,
                    "longitude" to longitude
                )
                Log.d("Location", "Bundle: $bundle")
                findNavController().navigate(R.id.qrFragment, bundle)
            } else {
                Log.d("Location", "Latitude: $latitude, Longitude: $longitude")
                showCustomToast(Status.Warn, "Location data not available", this@HomeFragment)
            }
        } else if (binding.withoutLocation.isChecked) {
            val bundle = bundleOf(
                "edtData" to data,
                "latitude" to "",
                "longitude" to ""
            )
            Log.d("Location", "Bundle: $bundle")
            findNavController().navigate(R.id.qrFragment, bundle)
        } else {
            showCustomToast(Status.Warn, "Information cannot be sent", this@HomeFragment)
        }
    }



    private inner class MyLocationListener : LocationListener {
        override fun onLocationChanged(location: Location) {
            this@HomeFragment.location = location // Update the location variable
            this@HomeFragment.latitude = location.latitude.toString()
            this@HomeFragment.longitude = location.longitude.toString()
            Log.d("Location", "Latitude: $latitude, Longitude: $longitude")
        }

        override fun onProviderEnabled(provider: String) {}

        override fun onProviderDisabled(provider: String) {}

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }


    private fun getLocation() {
        // Check for location permission
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Register the location listener for updates
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0f,
                locationListener
            )

            // Get the last known location
            val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastKnownLocation != null) {
                location = lastKnownLocation
                Log.d("Location", "Latitude: ${location?.latitude}, Longitude: ${location?.longitude}")
            } else {
                Log.d("Location", "Last known location is null")
            }
        } else {
            // Request location permission if not granted
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permission granted
                getLocation()
            } else {
                showCustomToast(Status.Warn, "Permission required.", this@HomeFragment)

            }
        }
    }

}