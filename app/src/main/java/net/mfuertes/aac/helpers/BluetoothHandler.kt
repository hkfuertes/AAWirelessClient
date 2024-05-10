package net.mfuertes.aac.helpers

import android.Manifest
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.util.*

class BluetoothHandler(context: Context, activityResultCaller: ActivityResultCaller?) {
    data class BluetoothDeviceInfo(val address: String, val name: String?)

    private val mContext = context
    private val mActivityResultCaller = activityResultCaller

    private var mBluetoothAdapter: BluetoothAdapter? = null

    private var mRequestPermissionsCallback: ((Boolean) -> Unit)? = null
    private val mRequestPermissionsLauncher =
        mActivityResultCaller?.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            val callback = mRequestPermissionsCallback
            mRequestPermissionsCallback = null

            if (isGranted) {
                callback?.invoke(true)
            } else {
                callback?.invoke(false)
            }
        }


    init {
        val bluetoothManager: BluetoothManager =
            mContext.getSystemService(BluetoothManager::class.java)
        mBluetoothAdapter = bluetoothManager.adapter
    }

    fun hasConnectPermissions(): Boolean {
        return android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S
                || mContext.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    }

    fun requestConnectPermissions(callback: ((success: Boolean) -> Unit)?) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) {
            callback?.invoke(true)
            return
        }

        mRequestPermissionsLauncher?.let {
            mRequestPermissionsCallback = callback
            it.launch(Manifest.permission.BLUETOOTH_CONNECT)
        }
    }

    fun getBondedDevices(): List<BluetoothDeviceInfo> {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && mContext.checkSelfPermission(
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return listOf()
        }

        mBluetoothAdapter?.let { adapter ->
            return adapter.bondedDevices.map {
                BluetoothDeviceInfo(it.address, it.name)
            }
        }

        return listOf()
    }


}
