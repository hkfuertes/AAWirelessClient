package net.mfuertes.aac.helpers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts


class WifiPermissionHandler(context: Context, activityResultCaller: ActivityResultCaller?) {
    private val mContext = context
    private val mActivityResultCaller = activityResultCaller

    private var mRequestPermissionsCallback: ((Boolean) -> Unit)? = null
    private val mRequestPermissionsLauncher = mActivityResultCaller?.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) { isGranted: Map<String, Boolean> ->
        val callback = mRequestPermissionsCallback
        mRequestPermissionsCallback = null

        if (isGranted.all { e -> e.value }) {
            callback?.invoke(true)
        }
        else {
            callback?.invoke(false)
        }
    }

    fun hasLocationPermissions(): Boolean {
        return mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && mContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun hasBackgroundLocationPermission(): Boolean {
        return mContext.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun requestLocationPermissions(callback: ((success: Boolean) -> Unit)?) {
        mRequestPermissionsLauncher?.let {
            mRequestPermissionsCallback = callback
            it.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    fun requestBackgroundLocationPermissions(callback: ((success: Boolean) -> Unit)?) {
        mRequestPermissionsLauncher?.let {
            mRequestPermissionsCallback = callback
            it.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
        }
    }
}