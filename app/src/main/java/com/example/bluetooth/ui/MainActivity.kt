package com.example.bluetooth.ui

import android.Manifest.permission.*
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.*
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bluetooth.BuildConfig
import com.example.bluetooth.data.model.Device
import com.example.bluetooth.data.service.BluetoothService
import com.example.bluetooth.databinding.ActivityMainBinding
import com.example.bluetooth.util.AppPermission.getPermissionList
import com.example.bluetooth.util.repeatOnStarted
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


private const val REQUEST_ENABLE_BT = 2
private const val REQUEST_DISCOVER_BT = 1

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val viewModel: BluetoothViewModel by viewModels()

    private lateinit var itemAdapter: DeviceListAdapter

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { isGranted ->
            val list = mutableListOf<String>()
            isGranted.forEach { (permission, state) ->
                if (state) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                        Toast.makeText(this, "권한 설정을 하지 않으면 어플을 사용할 수 없습니다.", Toast.LENGTH_SHORT)
                            .show()
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID))
                        startActivity(intent)
                    }
                } else {
                    list.add(permission)
                }
            }
            requestPermissions(list.toTypedArray(), 3)
        }

    private val bluetoothEnableLaunch: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.i("AppTest", "bluetoothEnableLaunch : ${result.resultCode}")
            if (result.resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "블루투스를 활성화하였습니다.", Toast.LENGTH_SHORT)
            } else {
                Toast.makeText(this, "블루투스를 활성화해주세요.", Toast.LENGTH_SHORT)
            }
        }

    private val connectListener: (String) -> Unit = { address ->
        viewModel.connectListener(address)
    }

    private val disconnectListener: (String) -> Unit = { address ->
        viewModel.disconnectListener(address)
    }

    private val permission = getPermissionList()

    private fun backgroundPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermissions(
                this,
                arrayOf(
                    ACCESS_BACKGROUND_LOCATION,
                ), 3
            )
        }
    }

    private fun permissionDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("백그라운드 위치 권한을 항상 허용으로 설정해주세요.")

        val listener = DialogInterface.OnClickListener { _, p1 ->
            when (p1) {
                DialogInterface.BUTTON_POSITIVE ->
                    backgroundPermission()
            }
        }
        builder.setPositiveButton("네", listener)
        builder.setNegativeButton("아니오", null)

        builder.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            finish()
        }

        itemAdapter = DeviceListAdapter(connectListener, disconnectListener)

        binding.rvDeviceList.apply {
            adapter = itemAdapter
        }

        repeatOnStarted {
            viewModel.devices.collect {
                itemAdapter.submitList(it)
            }
        }

        repeatOnStarted {
            viewModel.bluetoothState.collect {
                if (it) {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    enableBtIntent.putExtra("requestCode", -1)
                    bluetoothEnableLaunch.launch(enableBtIntent)
                }
            }
        }

        repeatOnStarted {
            viewModel.permission.collect {
                if(it) {
                    requestPermissionLauncher.launch(permission)
                }
            }
        }

        repeatOnStarted {
            viewModel.activityState.collect {
                if(it) {
                    finish()
                }
            }
        }

        if (
            permission.all { permission ->
                ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            }
        ) {
            if (checkSelfPermission(ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_DENIED) {
                permissionDialog(this)
            }

            viewModel.setBluetoothService()
            viewModel.setBindBluetoothService()
            viewModel.scanBluetooth()

        } else {
            requestPermissionLauncher.launch(permission)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.unregisterReceiver()
    }

}