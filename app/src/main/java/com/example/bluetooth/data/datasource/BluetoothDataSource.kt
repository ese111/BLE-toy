package com.example.bluetooth.data.datasource

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
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import com.example.bluetooth.data.model.Device
import com.example.bluetooth.data.service.BluetoothService
import com.example.bluetooth.util.AppPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.internal.notifyAll
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val _devices = MutableStateFlow<Set<Device>>(emptySet())
    val devices = _devices.asStateFlow()

    private val _activityFinish = MutableStateFlow(false)
    val activityFinish = _activityFinish.asStateFlow()

    private val _bluetoothState = MutableStateFlow<Boolean>(false)
    val bluetoothState = _bluetoothState.asStateFlow()

    private val _permission = MutableStateFlow<Boolean>(false)
    val permission = _permission.asStateFlow()

    private val bluetoothAdapter: BluetoothAdapter =
        context.getSystemService(BluetoothManager::class.java).adapter

    private lateinit var bluetoothService: BluetoothService


    private val scanCallback: ScanCallback = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val device: BluetoothDevice = result.device

            if (
                AppPermission.getPermissionList().all {
                    checkSelfPermission(
                        context,
                    it) != PackageManager.PERMISSION_GRANTED
                }
            ){
                return
            }
            Log.i(ContentValues.TAG, "deviceName : ${device.name}")
            Log.i(ContentValues.TAG, "deviceHardwareAddress : ${device.address}")
            addNewDeviceList(Device(device.name.orEmpty(), device.address.orEmpty()))
        }

    }

    private var connected = false

    private val gattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothService.ACTION_GATT_CONNECTED -> {
                    connected = true
                    Log.i(ContentValues.TAG, "ACTION_GATT_CONNECTED : $connected")
                }
                BluetoothService.ACTION_GATT_DISCONNECTED -> {
                    connected = false
                    Log.i(ContentValues.TAG, "ACTION_GATT_DISCONNECTED : $connected")
                }
            }
        }
    }

    private fun makeGattUpdateIntentFilter(): IntentFilter {
        return IntentFilter().apply {
            addAction(BluetoothService.ACTION_GATT_CONNECTED)
            addAction(BluetoothService.ACTION_GATT_DISCONNECTED)
        }
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            componentName: ComponentName,
            service: IBinder
        ) {
            Log.e(ContentValues.TAG, "bluetoothService")
            bluetoothService = (service as BluetoothService.LocalBinder).getService()
            Log.e(ContentValues.TAG, "Bluetooth initialize")
            bluetoothService.let { bluetooth ->
                if (!bluetooth.initialize()) {
                    Log.e(ContentValues.TAG, "Unable to initialize Bluetooth")
                    _activityFinish.value = true
                }
                // perform device connection
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Log.e(ContentValues.TAG, "onServiceDisconnected")
        }
    }

    fun connectListener(address: String) {
        val result = bluetoothService.connect(address)
        Log.d(ContentValues.TAG, "Connect request result = $result")
    }

    fun disconnectListener() {
        val result = bluetoothService.disconnect()
        Log.d(ContentValues.TAG, "Disconnect request result = $result")
    }

    fun scanBluetooth() {
        if (
            AppPermission.getPermissionList().all {
                checkSelfPermission(
                    context,
                    it
                ) != PackageManager.PERMISSION_GRANTED
            }
        ) {
            _permission.value = true
            return
        }
        _permission.value = false

        if (bluetoothAdapter.isEnabled) {
            val pairedDevices = bluetoothAdapter.bondedDevices
            Log.e(ContentValues.TAG, "${pairedDevices.size}")
            if (pairedDevices.size > 0) {
                for (device in pairedDevices) {
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address
                    addNewDeviceList(Device(deviceName, deviceHardwareAddress))
                    Log.i(ContentValues.TAG, "deviceName : $deviceName")
                    Log.i(ContentValues.TAG, "deviceHardwareAddress : $deviceHardwareAddress")
                }
            }
            bluetoothAdapter.bluetoothLeScanner.startScan(scanCallback)
            _bluetoothState.value = false
        } else {
            _bluetoothState.value = true
        }
    }

    fun setBindBluetoothService() {
        val gattServiceIntent = Intent(context, BluetoothService::class.java)
        context.bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        context.registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())
    }

    fun stopScan() {
        if (
            AppPermission.getPermissionList().all {
                checkSelfPermission(
                    context,
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }
        ) {
            bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
        }
    }

    fun unregisterReceiver() {
        context.unregisterReceiver(gattUpdateReceiver)
    }

    private fun addNewDeviceList(device: Device) {
        val set = mutableSetOf<Device>()
        set.addAll(_devices.value)
        set.add(device)
        _devices.value = set
    }

    fun clear() {
        _devices.value = emptySet()
    }

}