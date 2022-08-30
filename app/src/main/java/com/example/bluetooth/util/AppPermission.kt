package com.example.bluetooth.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi

object AppPermission {

    private val permission = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    @RequiresApi(Build.VERSION_CODES.S)
    private val permissionS = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_ADVERTISE
    )

    fun getPermissionList() = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            permissionS
        }
        else -> {
            permission
        }
    }

}