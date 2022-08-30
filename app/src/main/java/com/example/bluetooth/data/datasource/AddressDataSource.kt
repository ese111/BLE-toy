package com.example.bluetooth.data.datasource

import com.example.bluetooth.data.model.MapResponse
import com.example.bluetooth.data.network.AddressApi
import com.example.bluetooth.util.DefaultPosition
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddressDataSource @Inject constructor(
    private val api: AddressApi,
    private val defaultPosition: DefaultPosition
) {

    suspend fun getPosition(search: String) = api.getPosition(search = search)

    fun setDefaultPosition(info: MapResponse) {
        defaultPosition.position = info
    }

}