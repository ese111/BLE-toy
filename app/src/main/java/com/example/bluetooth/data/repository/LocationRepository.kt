package com.example.bluetooth.data.repository

import com.example.bluetooth.data.datasource.LocationDataSource
import com.example.bluetooth.data.model.MapResponse
import com.example.bluetooth.data.model.UserPosition
import com.example.bluetooth.util.DefaultPosition
import javax.inject.Inject

class LocationRepository @Inject constructor(
    private val dataSource: LocationDataSource
) {

    fun getLocation() = dataSource.getLocation()

//    fun getDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double) =
//        dataSource.getDistance(lat1, lon1, lat2, lon2)

    fun isNear(defaultPosition: MapResponse, location: UserPosition) = dataSource.isNear(defaultPosition, location)
}