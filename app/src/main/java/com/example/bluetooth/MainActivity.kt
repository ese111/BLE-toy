package com.example.bluetooth

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
import com.example.bluetooth.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.*


private const val REQUEST_ENABLE_BT = 2
private const val REQUEST_DISCOVER_BT = 1

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val viewModel: BluetoothViewModel by viewModels()

    private val permission = arrayOf(
        ACCESS_FINE_LOCATION
    )

    private val permissionS = arrayOf(
        BLUETOOTH_SCAN,
        ACCESS_FINE_LOCATION,
        BLUETOOTH_CONNECT,
        BLUETOOTH_ADVERTISE
    )

    private val permissionQ = arrayOf(
        ACCESS_FINE_LOCATION,
    )

    private val locationPermission = arrayOf(
        ACCESS_FINE_LOCATION,
    )

    private val bluetoothPermission = arrayOf(
        BLUETOOTH_SCAN,
        BLUETOOTH_CONNECT,
        BLUETOOTH_ADVERTISE
    )

    private lateinit var itemAdapter: DeviceListAdapter

    private val devices = MutableStateFlow<List<Device>>(emptyList())

    private lateinit var bluetoothAdapter: BluetoothAdapter

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { isGranted ->
            val list = mutableListOf<String>()
            isGranted.forEach { (t, u) ->
                if (u) {
                    Log.d("requestPermissionLauncher1", "$t, $u")
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, t)) {
                        Toast.makeText(this, "권한 설정을 하지 않으면 어플을 사용할 수 없습니다.", Toast.LENGTH_SHORT)
                            .show()
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID))
                        startActivity(intent)
                    }
                } else {
                    Log.d("requestPermissionLauncher3", "$t, $u")
                    list.add(t)
                }
            }
            requestPermissions(list.toTypedArray(), 3)
        }

    private val bluetoothEnableLaunch: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.i("AppTest", "bluetoothEnableLaunch : ${result.resultCode}")
            if (result.resultCode == Activity.RESULT_OK) {

            }
        }

    private lateinit var connectListener: (String) -> Unit

    private lateinit var disconnectListener: (String) -> Unit

    var UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
    var TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")
    var CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    private val callback = object : BluetoothGattCallback(){
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            when(newState){
                BluetoothProfile.STATE_CONNECTING -> {

                }
                BluetoothProfile.STATE_CONNECTED -> {
                    if (checkSelfPermission(
                            BLUETOOTH_CONNECT
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        gatt?.discoverServices()
                    }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                }
                else -> {

                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            when(status){
                BluetoothGatt.GATT_SUCCESS -> {

                    val tx = gatt.getService(UART_UUID).getCharacteristic(TX_CHAR_UUID);

                    if (checkSelfPermission(
                            BLUETOOTH_CONNECT
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        if (gatt.setCharacteristicNotification(tx, true)) {
                            val descriptor: BluetoothGattDescriptor = tx.getDescriptor(CCCD_UUID)
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            gatt.writeDescriptor(descriptor)
                        }
                    }

                }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            val data = characteristic!!.value
            val str = String(data)
            Log.d("log",str)
        }
    }

    private fun backgroundPermission() {
        requestPermissions(
            this,
            arrayOf(
                ACCESS_BACKGROUND_LOCATION,
            ), 3
        )
    }

    private fun permissionDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("백그라운드 위치 권한을 위해 항상 허용으로 설정해주세요.")

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

    private var connected = false

    private var deviceAddress = ""

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            Log.i("AppTest", "onReceive")
            if (permissionS.all { permission ->
                    ContextCompat.checkSelfPermission(
                        context,
                        permission
                    ) == PackageManager.PERMISSION_GRANTED
                }
            ) {
                Log.i("AppTest", "${intent.action.toString()}")
                when (intent.action.orEmpty()) {
                    BluetoothDevice.ACTION_FOUND -> {
                        // Discovery has found a device. Get the BluetoothDevice
                        // object and its info from the Intent.
                        val device: BluetoothDevice =
                            requireNotNull(intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE))
                        val deviceName = device.name
                        val deviceHardwareAddress = device.address // MAC address
                        Log.i("AppTest", deviceName)
                        val list = mutableListOf<Device>()
                        list.addAll(devices.value)
                        list.add(Device(deviceName, deviceHardwareAddress))
                        devices.value = list
                    }
                }
            }
        }
    }

    private val gattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothService.ACTION_GATT_CONNECTED -> {
                    connected = true
                    Log.i(TAG, "ACTION_GATT_CONNECTED : $connected")
//                    updateConnectionState(R.string.connected)
                }
                BluetoothService.ACTION_GATT_DISCONNECTED -> {
                    connected = false
                    Log.i(TAG, "ACTION_GATT_DISCONNECTED : $connected")
//                    updateConnectionState(R.string.disconnected)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
//        unregisterReceiver(gattUpdateReceiver)
    }

    private fun makeGattUpdateIntentFilter(): IntentFilter {
        return IntentFilter().apply {
            addAction(BluetoothService.ACTION_GATT_CONNECTED)
            addAction(BluetoothService.ACTION_GATT_DISCONNECTED)
        }
    }

    private val scanCallback: ScanCallback = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val device: BluetoothDevice = result.device
            val list = mutableListOf<Device>()
            list.addAll(devices.value)
            Log.i("AppTest", "1 ${device.name}")

            if (checkSelfPermission(
                    BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.i("AppTest", "2 ${device.name}")

                list.add(Device(device.name, device.address))
                devices.value = list
                stopScan()
            }

        }
    }

    private lateinit var bluetoothService : BluetoothService

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            componentName: ComponentName,
            service: IBinder
        ) {
            Log.e(TAG, "bluetoothService")
            bluetoothService = (service as BluetoothService.LocalBinder).getService()
            Log.e(TAG, "Bluetooth initialize")
            bluetoothService.let { bluetooth ->
                if (!bluetooth.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth")
                    finish()
                }
                // perform device connection
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Log.e(TAG, "onServiceDisconnected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            finish()
        }
        Log.i(TAG, "gattServiceIntent")
        val gattServiceIntent = Intent(this, BluetoothService::class.java)
        bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())
        connectListener = { address ->

            val result = bluetoothService.connect(address)
            Log.d(TAG, "Connect request result=$result")
        }

        disconnectListener =  { address ->
//            registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())
            val result = bluetoothService.disconnect(address)
            Log.d(TAG, "Connect request result=$result")
        }

        itemAdapter = DeviceListAdapter(connectListener, disconnectListener)

        binding.rvDeviceList.apply {
            adapter = itemAdapter
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                devices.collect {
                    itemAdapter.submitList(it)
                }
            }
        }

        Log.i("AppTest", "Build.VERSION_CODES.S")
        if (
            getPermissionList().all { permission ->
                ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            }
        ) {
            if(checkSelfPermission(ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_DENIED) {
                permissionDialog(this)
            }

            Log.i("AppTest", "permissionS : true")
            val bluetoothManager = getSystemService(BluetoothManager::class.java)
            bluetoothAdapter = bluetoothManager.adapter

            if (bluetoothAdapter.isEnabled) {
                Log.i("AppTest", "isEnabled : true")
                val pairedDevices = bluetoothAdapter.bondedDevices
                Log.i("AppTest", "pairedDevices : ${pairedDevices.size}")
                if (pairedDevices.size > 0) {
                    // There are paired devices. Get the name and address of each paired device.
                    for (device in pairedDevices) {
                        val deviceName = device.name
                        val deviceHardwareAddress = device.address // MAC address
                    }
                }
                bluetoothAdapter.bluetoothLeScanner.startScan(scanCallback)
//                    bluetoothAdapter.startDiscovery()
//                    val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
//                    registerReceiver(receiver, filter)
            } else {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                enableBtIntent.putExtra("requestCode", -1)
                bluetoothEnableLaunch.launch(enableBtIntent)
            }
        } else {
            requestPermissionLauncher.launch(permissionS)
        }

    }

    private fun getPermissionList() = when (Build.VERSION.SDK_INT) {
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

    private fun stopScan() {
        if (checkSelfPermission(
                BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        unregisterReceiver(receiver)
    }

}