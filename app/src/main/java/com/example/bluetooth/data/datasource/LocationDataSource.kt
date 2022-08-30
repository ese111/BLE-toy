package com.example.bluetooth.data.datasource

import com.example.bluetooth.data.model.MapResponse
import com.example.bluetooth.data.model.UserPosition
import com.example.bluetooth.data.service.UserLocation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationDataSource @Inject constructor(
    private val location: UserLocation
) {
    fun setLocation() = location.getUserLocation()

    private fun getDistance(lon1: Double, lat1: Double, lon2: Double, lat2: Double) = location.getDistance(lat1, lon1, lat2, lon2)

    fun isNear(defaultPosition: MapResponse, location: UserPosition) =
        getDistance(defaultPosition.x, defaultPosition.y, location.longitude, location.latitude) / 1000 < 1

    fun getLocation() = location.location
}