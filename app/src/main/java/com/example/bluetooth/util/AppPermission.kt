package com.example.bluetooth.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi

object AppPermission {

    fun hasBluetoothConnectPermission(context: Context) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.checkSelfPermission(
        Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

    private val permission = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private val permissionS = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_ADVERTISE
    )

    private val permissionQ = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
    )

    fun getPermissionList() = when (Build.VERSION.SDK_INT) {
        Build.VERSION_CODES.Q -> {
            permissionQ
        }
        Build.VERSION_CODES.S -> {
            permissionS
        }
        else -> {
            permission
        }
    }

}