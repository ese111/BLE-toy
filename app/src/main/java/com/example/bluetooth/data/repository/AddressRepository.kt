package com.example.bluetooth.data.repository

import com.example.bluetooth.data.datasource.AddressDataSource
import com.example.bluetooth.data.dto.toMapResponse
import com.example.bluetooth.data.model.MapResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddressRepository @Inject constructor(
    private val dataSource: AddressDataSource
){

    suspend fun getPosition(search: String) = dataSource.getPosition(search).toMapResponse()

    fun setDefaultPosition(info: MapResponse) = dataSource.setDefaultPosition(info)
}