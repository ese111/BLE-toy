package com.example.bluetooth

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothRepository @Inject constructor(
    private val dataSource: BluetoothDataSource
){

}