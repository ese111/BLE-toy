package com.example.bluetooth.data.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.bluetooth.data.model.UserPosition
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Singleton
class UserLocation @Inject constructor(
    @ApplicationContext private val context: Context
){

    private val locationManager = context.getSystemService(LocationManager::class.java)

    // 삼각함수를 이용해서 거리계산
    fun getDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
        val EARTH_R = 6371000.0
        val rad = Math.PI / 180
        val radLat1 = rad * lat1
        val radLat2 = rad * lat2
        val radDist = rad * (lon1 - lon2)

        var distance = sin(radLat1) * sin(radLat2)
        distance += cos(radLat1) * cos(radLat2) * cos(radDist)
        val ret = EARTH_R * acos(distance)

        return ret.roundToInt() // 미터 단위
    }

    fun getUserLocation(): UserPosition {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val location : Location? = locationManager
                .getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                Log.d("Test", "GPS Location changed, Latitude: $latitude" +
                        ", Longitude: $longitude")
                return UserPosition(latitude, longitude)
            }
        }
        return UserPosition()
    }
}