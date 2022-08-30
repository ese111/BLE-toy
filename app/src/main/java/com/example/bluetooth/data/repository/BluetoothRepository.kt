package com.example.bluetooth.data.repository

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.*
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.bluetooth.data.datasource.BluetoothDataSource
import com.example.bluetooth.data.model.Device
import com.example.bluetooth.data.service.BluetoothService
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothRepository @Inject constructor(
    private val dataSource: BluetoothDataSource
){

    fun getDeviceList() = dataSource.devices

    fun getActivityFinish() = dataSource.activityFinish

    fun getBluetoothState() = dataSource.bluetoothState

    fun getPermissionState() = dataSource.permission

    fun connectListener(address: String) {
        dataSource.connectListener(address)
    }

    fun disconnectListener() {
        dataSource.disconnectListener()
    }

    fun scanBluetooth() = dataSource.scanBluetooth()

    fun setBindBluetoothService() {
        dataSource.setBindBluetoothService()
    }

    fun stopScan() {
        dataSource.stopScan()
    }

    fun unregisterReceiver() = dataSource.unregisterReceiver()

    fun clear() {
        dataSource.clear()
    }
}