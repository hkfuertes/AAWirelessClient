package net.mfuertes.aac.receivers

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import net.mfuertes.aac.services.WifiService

class BluetoothReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != BluetoothDevice.ACTION_ACL_CONNECTED) {
            return
        }

        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val bluetoothDevice = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

        if (bluetoothDevice != null
            && bluetoothDevice.address.equals(preferences.getString("gateway_bt_mac", null), true)
        ) {
            context.startForegroundService(Intent(context, WifiService::class.java))
        }
    }
}