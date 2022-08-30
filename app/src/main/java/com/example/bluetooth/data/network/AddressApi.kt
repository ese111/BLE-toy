package com.example.bluetooth.data.network

import com.example.bluetooth.BuildConfig
import com.example.bluetooth.data.dto.MapResponseDTO
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface AddressApi {

    @GET("map-geocode/v2/geocode")
    suspend fun getPosition(
        @Header("X-NCP-APIGW-API-KEY-ID") id: String = BuildConfig.CLIENT_ID,
        @Header("X-NCP-APIGW-API-KEY") key: String = BuildConfig.CLIENT_KEY,
        @Query("query") search: String
    ): MapResponseDTO

}
