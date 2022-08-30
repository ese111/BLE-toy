package com.example.bluetooth.util

import com.example.bluetooth.data.model.MapResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultPosition @Inject constructor() {
    var position = MapResponse()
}