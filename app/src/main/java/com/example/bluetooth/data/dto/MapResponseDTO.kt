package com.example.bluetooth.data.dto


import com.example.bluetooth.data.model.MapResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MapResponseDTO(
    @field:Json(name = "addresses")
    val addresses: List<Addresse?>?,
    @field:Json(name = "errorMessage")
    val errorMessage: String?,
    @field:Json(name = "meta")
    val meta: Meta?,
    @field:Json(name = "status")
    val status: String?
)

@JsonClass(generateAdapter = true)
data class AddressElement(
    @field:Json(name = "code")
    val code: String?,
    @field:Json(name = "longName")
    val longName: String?,
    @field:Json(name = "shortName")
    val shortName: String?,
    @field:Json(name = "types")
    val types: List<String?>?
)

@JsonClass(generateAdapter = true)
data class Meta(
    @field:Json(name = "count")
    val count: Int?,
    @field:Json(name = "page")
    val page: Int?,
    @field:Json(name = "totalCount")
    val totalCount: Int?
)

@JsonClass(generateAdapter = true)
data class Addresse(
    @field:Json(name = "addressElements")
    val addressElements: List<AddressElement?>?,
    @field:Json(name = "distance")
    val distance: Double?,
    @field:Json(name = "englishAddress")
    val englishAddress: String?,
    @field:Json(name = "jibunAddress")
    val jibunAddress: String?,
    @field:Json(name = "roadAddress")
    val roadAddress: String?,
    @field:Json(name = "x")
    val x: String?,
    @field:Json(name = "y")
    val y: String?
)

fun MapResponseDTO.toMapResponse(): List<MapResponse> {
    return addresses?.map {
        val x = requireNotNull(it?.x)
        val y = requireNotNull(it?.y)
        val roadAddress = requireNotNull(it?.roadAddress)
        val jibunAddress = requireNotNull(it?.jibunAddress)
        MapResponse(x.toDouble(), y.toDouble(), roadAddress, jibunAddress)
    } ?: listOf(MapResponse())
}