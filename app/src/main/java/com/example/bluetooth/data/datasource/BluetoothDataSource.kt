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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import com.example.bluetooth.data.model.Device
import com.example.bluetooth.data.service.BluetoothService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val _devices = MutableStateFlow<List<Device>>(emptyList())
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

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            addNewDeviceList(Device(device.name, device.address))
        }
    }

    private var connected = false

    private val gattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothService.ACTION_GATT_CONNECTED -> {
                    connected = true
                    Log.i(ContentValues.TAG, "ACTION_GATT_CONNECTED : $connected")
//                    updateConnectionState(R.string.connected)
                }
                BluetoothService.ACTION_GATT_DISCONNECTED -> {
                    connected = false
                    Log.i(ContentValues.TAG, "ACTION_GATT_DISCONNECTED : $connected")
//                    updateConnectionState(R.string.disconnected)
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
        Log.d(ContentValues.TAG, "Connect request result=$result")
    }

    fun disconnectListener(address: String) {
        val result = bluetoothService.disconnect(address)
        Log.d(ContentValues.TAG, "Connect request result=$result")
    }

    fun scanBluetooth() {
        if (
            checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            _permission.value = true
        }
        _permission.value = false

        if (bluetoothAdapter.isEnabled) {
            val pairedDevices = bluetoothAdapter.bondedDevices
            Log.e(ContentValues.TAG, "${pairedDevices.size}")
            if (pairedDevices.size > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (device in pairedDevices) {
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address
                    addNewDeviceList(Device(deviceName, deviceHardwareAddress))
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
            checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
        }
    }

    fun unregisterReceiver() {
        context.unregisterReceiver(gattUpdateReceiver)
    }

    private fun addNewDeviceList(device: Device) {
        val list = mutableListOf<Device>()
        list.addAll(_devices.value)
        list.add(device)
        _devices.value = list
    }

}