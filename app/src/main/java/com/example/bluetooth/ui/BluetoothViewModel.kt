package com.example.bluetooth.ui

import android.Manifest
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.data.model.Device
import com.example.bluetooth.data.repository.BluetoothRepository
import com.example.bluetooth.data.service.BluetoothService
import com.example.bluetooth.util.AppPermission
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val repository: BluetoothRepository
) : ViewModel() {

    private val _devices = MutableStateFlow<List<Device>>(emptyList())
    val devices = _devices.asStateFlow()

    private val _bluetoothState = MutableStateFlow(false)
    val bluetoothState = _bluetoothState.asStateFlow()

    private val _permission = MutableStateFlow(false)
    val permission = _permission.asStateFlow()

    private val _activityState = MutableStateFlow(false)
    val activityState = _activityState.asStateFlow()

    fun scanBluetooth() = repository.scanBluetooth()

    init {
        viewModelScope.launch {
            repository.getPermissionState().collect {
                _permission.value = it
            }
        }

        viewModelScope.launch {
            repository.getBluetoothState().collect {
                _bluetoothState.value = it
            }
        }

        viewModelScope.launch {
            repository.getDeviceList().collect {
                _devices.value = it
            }
        }

        viewModelScope.launch {
            repository.getActivityFinish().collect {
                _activityState.value = it
            }
        }
    }


    fun connectListener(address: String) = repository.connectListener(address)

    fun disconnectListener(address: String) = repository.disconnectListener(address)

    fun setBindBluetoothService() = repository.setBindBluetoothService()

    fun setBluetoothService() = repository.setBindBluetoothService()

    fun stopScan() = repository.stopScan()

    fun unregisterReceiver() = repository.unregisterReceiver()

}