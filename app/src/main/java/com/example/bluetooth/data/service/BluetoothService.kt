package com.example.bluetooth.data.service

import android.app.Service
import android.bluetooth.*
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.bluetooth.util.AppPermission
import javax.inject.Singleton

@Singleton
class BluetoothService: Service() {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private val binder = LocalBinder()

    private var bluetoothGatt: BluetoothGatt? = null

    private var connectionState = STATE_DISCONNECTED

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                connectionState = STATE_CONNECTED
                broadcastUpdate(ACTION_GATT_CONNECTED)
                Log.i(TAG, "onConnectionStateChange")
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                connectionState = STATE_DISCONNECTED
                broadcastUpdate(ACTION_GATT_DISCONNECTED)
                Log.i(TAG, "onConnectionStateChange")
            }
        }
    }

    fun initialize(): Boolean {
        bluetoothAdapter = getSystemService(BluetoothManager::class.java).adapter
        if (bluetoothAdapter == null) {
            return false
        }
        return true
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun connect(address: String): Boolean {
        bluetoothAdapter?.let { adapter ->
            try {
                val device = adapter.getRemoteDevice(address)
                if (AppPermission.getPermissionList().all {
                        ContextCompat.checkSelfPermission(
                            this,
                            it
                        ) == PackageManager.PERMISSION_GRANTED
                    }
                ) {
                    bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback)
                    Log.w(TAG, "connect > ${bluetoothGatt?.device}")
                }
            } catch (exception: IllegalArgumentException) {
                Log.w(TAG, "Device not found with provided address.")
                return false
            }
            // connect to the GATT server on the device
        } ?: run {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return false
        }
        return  true
    }

    fun disconnect(): Boolean {
        bluetoothGatt?.let {
            if (AppPermission.getPermissionList().all { permission ->
                    ContextCompat.checkSelfPermission(
                        this,
                        permission
                    ) == PackageManager.PERMISSION_DENIED
                }
            ) {
                return false
            }
            Log.i("Disconnect", "disconnect : ${it.device.name}, ${it.device.address}")
            it.disconnect()
        }
        return true
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): BluetoothService = this@BluetoothService
    }

    override fun onUnbind(intent: Intent?): Boolean {
        close()
        return super.onUnbind(intent)
    }

    private fun close() {
        bluetoothGatt?.let { gatt ->
            if (AppPermission.getPermissionList().all {
                    ContextCompat.checkSelfPermission(
                        this,
                        it
                    ) != PackageManager.PERMISSION_GRANTED
                }
            ) {
                return
            }
            gatt.close()
            bluetoothGatt = null
        }
    }

    companion object {
        const val ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"

        private const val STATE_DISCONNECTED = 0
        private const val STATE_CONNECTED = 2

    }

}