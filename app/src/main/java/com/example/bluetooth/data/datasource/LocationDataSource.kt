package com.example.bluetooth.data.datasource

import com.example.bluetooth.data.model.MapResponse
import com.example.bluetooth.data.model.UserPosition
import com.example.bluetooth.data.service.UserLocation
import com.example.bluetooth.util.DefaultPosition
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationDataSource @Inject constructor(
    private val location: UserLocation
) {
    fun getLocation() = flow {
        emit(location.getUserLocation())
    }

    private fun getDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double) = location.getDistance(lat1, lon1, lat2, lon2)

    fun isNear(defaultPosition: MapResponse, location: UserPosition) =
        getDistance(defaultPosition.x, defaultPosition.y, location.latitude, location.longitude) / 1000 > 1
}