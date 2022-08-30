package com.example.bluetooth.data.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.bluetooth.data.model.UserPosition
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Singleton
class UserLocation @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val _location = MutableStateFlow(UserPosition())
    val location = _location.asStateFlow()

    // 삼각함수를 이용해서 거리계산
    fun getDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
        val EARTH_R = 6371000.0
        val rad = Math.PI / 180
        val radLat1 = rad * lat1
        val radLat2 = rad * lat2
        val radDist = rad * (lon1 - lon2)
        Log.d("Location", "myLocation: $lat2, $lon2")
        Log.d("Location", "Location: $lat1, $lon1")
        var distance = sin(radLat1) * sin(radLat2)
        distance += cos(radLat1) * cos(radLat2) * cos(radDist)
        val ret = EARTH_R * acos(distance)
        Log.d("Location", "roundToInt: ${ret.roundToInt()}")
        return ret.roundToInt() // 미터 단위
    }

    fun getUserLocation() {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            var latitude = 1.0
            var longitude = 1.0
            Log.d("Test", "GPS1 ${longitude}")
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // Got last known location. In some rare situations this can be null.
                    Log.d("Test", "GPS ${location?.longitude}")
                    if (location != null) {
                        latitude = location.latitude
                        longitude = location.longitude
                        Log.d(
                            "Test", "GPS Location changed, Latitude: $latitude" +
                                    ", Longitude: $longitude"
                        )
                    }
                    _location.value = UserPosition(latitude, longitude)
                }
        }
    }

}