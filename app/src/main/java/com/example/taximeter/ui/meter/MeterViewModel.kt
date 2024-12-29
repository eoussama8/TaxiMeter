package com.example.taximeter.ui.meter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import kotlin.math.roundToInt

class MeterViewModel : ViewModel() {

    companion object {
        private const val BASE_FARE = 2.0
        private const val PER_KM_RATE = 2.0
        private const val PER_MINUTE_RATE = 1.0
        private const val ROUND_TO_DECIMALS = 2
    }

    private val _totalDistance = MutableLiveData(0.0)
    val totalDistance: LiveData<Double> = _totalDistance

    private val _totalTime = MutableLiveData(0)
    val totalTime: LiveData<Int> = _totalTime

    private val _fare = MutableLiveData(BASE_FARE)
    val fare: LiveData<Double> = _fare

    // Additional states
    private val _isRunning = MutableLiveData(false)
    val isRunning: LiveData<Boolean> = _isRunning

    // Derived states
    val formattedDistance = totalDistance.map { distance ->
        "%.${ROUND_TO_DECIMALS}f".format(distance)
    }

    val averageSpeed = totalDistance.map { distance ->
        if (_totalTime.value != 0) {
            (distance / (_totalTime.value!! / 60.0)).roundToDecimals(ROUND_TO_DECIMALS)
        } else 0.0
    }

    fun resetMeter() {
        _totalDistance.value = 0.0
        _totalTime.value = 0
        _fare.value = BASE_FARE
        _isRunning.value = true
    }

    fun updateDistance(additionalDistance: Double) {
        _totalDistance.value = (_totalDistance.value ?: 0.0) + additionalDistance
        updateFare()
    }

    fun incrementTime() {
        _totalTime.value = (_totalTime.value ?: 0) + 1
        updateFare()
    }

    fun setTotalDistance(distance: Double) {
        _totalDistance.value = distance
        updateFare()
    }

    fun setTotalTime(time: Int) {
        _totalTime.value = time
        updateFare()
    }

    fun stopMeter() {
        _isRunning.value = false
    }

    private fun updateFare() {
        val newFare = calculateFare(
            _totalDistance.value ?: 0.0,
            _totalTime.value ?: 0
        )
        _fare.value = newFare.roundToDecimals(ROUND_TO_DECIMALS)
    }

    private fun calculateFare(distance: Double, time: Int): Double {
        return BASE_FARE + (distance * PER_KM_RATE) + (time * PER_MINUTE_RATE)
    }

    private fun Double.roundToDecimals(decimals: Int): Double {
        val factor = Math.pow(10.0, decimals.toDouble())
        return (this * factor).roundToInt() / factor
    }

    fun getTripSummary(): TripSummary {
        return TripSummary(
            distance = _totalDistance.value ?: 0.0,
            time = _totalTime.value ?: 0,
            fare = _fare.value ?: BASE_FARE,
            averageSpeed = averageSpeed.value ?: 0.0
        )
    }

    data class TripSummary(
        val distance: Double,
        val time: Int,
        val fare: Double,
        val averageSpeed: Double
    )
}