package com.example.bluetooth.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.data.model.Device
import com.example.bluetooth.data.model.UserPosition
import com.example.bluetooth.data.repository.BluetoothRepository
import com.example.bluetooth.data.repository.LocationRepository
import com.example.bluetooth.util.DefaultPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val bluetoothRepository: BluetoothRepository,
    private val locationRepository: LocationRepository,
    private val defaultPosition: DefaultPosition
) : ViewModel() {

    private val _devices = MutableStateFlow<List<Device>>(emptyList())
    val devices = _devices.asStateFlow()

    private val _bluetoothState = MutableStateFlow(false)
    val bluetoothState = _bluetoothState.asStateFlow()

    private val _permission = MutableStateFlow(false)
    val permission = _permission.asStateFlow()

    private val _activityState = MutableStateFlow(false)
    val activityState = _activityState.asStateFlow()

    private val _location = MutableStateFlow(UserPosition())
    val location = _location.asStateFlow()

    fun scanBluetooth() = bluetoothRepository.scanBluetooth()

    init {
        viewModelScope.launch {
            bluetoothRepository.getPermissionState().collect {
                _permission.value = it
            }
        }

        viewModelScope.launch {
            bluetoothRepository.getBluetoothState().collect {
                _bluetoothState.value = it
            }
        }

        viewModelScope.launch {
            bluetoothRepository.getDeviceList().collect {
                _devices.value = it.toList()
            }
        }

        viewModelScope.launch {
            bluetoothRepository.getActivityFinish().collect {
                _activityState.value = it
            }
        }

        viewModelScope.launch {
            locationRepository.getLocation().collect {
                Log.d("ViewModel", "getLocation : $it")
                _location.value = it
            }
        }
    }

    fun setLocation() = locationRepository.setLocation()

    fun isNear(location: UserPosition) = locationRepository.isNear(defaultPosition.position, location)

    fun connectListener(address: String) = bluetoothRepository.connectListener(address)

    fun disconnectListener() = bluetoothRepository.disconnectListener()

    fun setBindBluetoothService() {
        bluetoothRepository.setBindBluetoothService()
    }

    fun setBluetoothService() {
        bluetoothRepository.setBindBluetoothService()
    }

    fun stopScan() = bluetoothRepository.stopScan()

    fun unregisterReceiver() = bluetoothRepository.unregisterReceiver()

    fun clear() {
        _devices.value = emptyList()
        bluetoothRepository.clear()
    }
}