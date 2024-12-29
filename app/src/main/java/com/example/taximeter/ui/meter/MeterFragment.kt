package com.example.taximeter.ui.meter

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.taximeter.R
import com.example.taximeter.databinding.FragmentMeterBinding
import com.example.taximeter.ui.history.HistoryFragment
import com.example.taximeter.ui.history.HistoryItem
import com.example.taximeter.ui.history.HistoryViewModel
import com.google.android.gms.location.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MeterFragment : Fragment() {

    private var _binding: FragmentMeterBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var lastLocation: Location? = null
    private var isMeterRunning = false

    private val historyViewModel: HistoryViewModel by activityViewModels()
    private val meterViewModel: MeterViewModel by activityViewModels()

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val LOCATION_UPDATE_INTERVAL = 5000L
        private const val LOCATION_FASTEST_INTERVAL = 2000L
        private const val TIME_UPDATE_INTERVAL = 60000L // 1 minute
        private const val BASE_FARE = 2.0
        private const val PER_KM_RATE = 2.0
        private const val PER_MINUTE_RATE = 1.0
        private const val DATE_FORMAT = "MMMM dd, yyyy • HH:mm"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMeterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeLocationServices()
        setupUI()
        observeViewModel()
        restoreState(savedInstanceState)
    }

    private fun initializeLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        setupLocationCallback()
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { newLocation ->
                    updateDistance(newLocation)
                }
            }
        }
    }

    private fun setupUI() {
        with(binding) {
            startButton.setOnClickListener { if (!isMeterRunning) startMeter() }
            stopButton.setOnClickListener { if (isMeterRunning) stopMeter() }
        }
    }

    private fun formatDistance(distance: Double): String = getString(R.string.distance_display, distance)
    private fun formatTime(time: Int): String = getString(R.string.time_display, time)
    private fun formatFare(fare: Double): String = getString(R.string.fare_display, fare)


    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            meterViewModel.apply {
                totalDistance.observe(viewLifecycleOwner) { distance ->
                    binding.distanceText.text = formatDistance(distance)
                }
                totalTime.observe(viewLifecycleOwner) { time ->
                    binding.timeText.text = formatTime(time)
                }
                fare.observe(viewLifecycleOwner) { fare ->
                    binding.fareText.text = formatFare(fare)
                }
            }
        }
    }



    private fun startMeter() {
        if (!checkLocationPermission()) {
            requestLocationPermission()
            return
        }

        isMeterRunning = true
        meterViewModel.resetMeter()
        startLocationUpdates()
        startTimeUpdates()
    }

    private fun stopMeter() {
        isMeterRunning = false
        stopLocationUpdates()
        saveRideHistory()
        navigateToHistory()
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = LOCATION_UPDATE_INTERVAL
            fastestInterval = LOCATION_FASTEST_INTERVAL
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e("MeterFragment", "Location permission not granted", e)
        }
    }

    private fun startTimeUpdates() {
        viewLifecycleOwner.lifecycleScope.launch {
            while (isMeterRunning) {
                delay(TIME_UPDATE_INTERVAL)
                meterViewModel.incrementTime()
            }
        }
    }

    private fun updateDistance(newLocation: Location) {
        lastLocation?.let { previousLocation ->
            val distance = calculateDistance(
                previousLocation.latitude,
                previousLocation.longitude,
                newLocation.latitude,
                newLocation.longitude
            )
            meterViewModel.updateDistance(distance)
        }
        lastLocation = newLocation
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }

    private fun saveRideHistory() {
        val currentDateTime = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            .format(Date())

        val historyItem = HistoryItem(
            dateTime = currentDateTime,
            startLocation = lastLocation?.let { "${it.latitude}, ${it.longitude}" } ?: "Unknown",
            endLocation = "End Location",
            fare = formatFare(meterViewModel.fare.value ?: 0.0),
            distance = formatDistance(meterViewModel.totalDistance.value ?: 0.0)
        )

        historyViewModel.addHistoryItem(historyItem)
    }

    private fun navigateToHistory() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HistoryFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun stopLocationUpdates() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            Log.e("MeterFragment", "Error stopping location updates", e)
        }
    }

    private fun restoreState(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            isMeterRunning = it.getBoolean("isMeterRunning", false)
            if (isMeterRunning) {
                meterViewModel.apply {
                    setTotalDistance(it.getDouble("totalDistance", 0.0))
                    setTotalTime(it.getInt("totalTime", 0))
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {
            putDouble("totalDistance", meterViewModel.totalDistance.value ?: 0.0)
            putInt("totalTime", meterViewModel.totalTime.value ?: 0)
            putBoolean("isMeterRunning", isMeterRunning)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isMeterRunning) {
            stopLocationUpdates()
        }
        _binding = null
    }
}