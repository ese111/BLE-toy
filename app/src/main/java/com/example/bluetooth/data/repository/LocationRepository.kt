package com.example.bluetooth.data.repository

import com.example.bluetooth.data.datasource.LocationDataSource
import com.example.bluetooth.data.model.MapResponse
import com.example.bluetooth.data.model.UserPosition
import javax.inject.Inject

class LocationRepository @Inject constructor(
    private val dataSource: LocationDataSource
) {

    fun setLocation() = dataSource.setLocation()

    fun isNear(defaultPosition: MapResponse, location: UserPosition) = dataSource.isNear(defaultPosition, location)

    fun getLocation() = dataSource.getLocation()
}