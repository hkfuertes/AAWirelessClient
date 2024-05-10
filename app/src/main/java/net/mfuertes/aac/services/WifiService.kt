package net.mfuertes.aac.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.SupplicantState
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import net.mfuertes.aac.AndroidAutoHelper
import net.mfuertes.aac.R
import net.mfuertes.aac.helpers.ProxyThread

// https://github.com/yatapone/SampleWifiConnector/blob/main/app/src/main/java/com/yatapone/samplewificonnector/MainActivity.kt
class WifiService : Service() {

    companion object {
        const val TAG = "WifiService"
        private const val NOTIFICATION_CHANNEL_ID = "default"
        private const val NOTIFICATION_ID = 2

        private const val CANCEL_ACTION = "ACTION_WIFISERVICE_CANCEL"
    }

    private lateinit var wifiManager: WifiManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var mProxyThread: ProxyThread

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onCreate() {
        wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val notificationManager = getSystemService(NotificationManager::class.java)

        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "General",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationChannel.setSound(null, null)
        notificationChannel.enableVibration(false)
        notificationChannel.enableLights(false)
        notificationManager.createNotificationChannel(notificationChannel)
        super.onCreate()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if(intent?.action == CANCEL_ACTION) {
            stopSelf()
            return START_STICKY
        }

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)

        val ssid = preferences.getString("gateway_wifi_ssid", null)
        val password = preferences.getString("gateway_wifi_password", null)

        if (ssid == null || password == null) {
            return START_STICKY
        }

        updateNotification("Started")
        connectByWifiNetworkSpecifier(ssid, password){ address, _, _ ->
            mProxyThread = ProxyThread(address){
                AndroidAutoHelper.connectLocalhostAAWireless(this)
            }

            mProxyThread.start()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        mProxyThread.exit()
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            Log.d(TAG, "onDestroy: unregisterNetworkCallback: e=$e")
        }
        stopForeground(false)
        super.onDestroy()
    }

    private fun updateNotification(msg: String) {
        val intent = Intent(this, WifiService::class.java).apply{
            action = CANCEL_ACTION
        }
        val pendingIntent =
            PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        Log.i(TAG, "Notification updated: $msg")
        val notificationBuilder = Notification.Builder(
            this,
            NOTIFICATION_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.android_auto)
            .setContentTitle("Android Auto")
            .setContentText(msg)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(0, "Exit", pendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            notificationBuilder.setFlag(Notification.FLAG_NO_CLEAR, true)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            notificationBuilder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
        }

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun connectByWifiNetworkSpecifier(
        ssid: String,
        pass: String,
        callback: (address: String, network: Network?, wifiInfo: WifiInfo?) -> Unit
    ) {
        Log.d(TAG, "connectByWifiNetworkSpecifier: wifi=$ssid, pass=$pass")
        val specifier = WifiNetworkSpecifier.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(pass)

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .setNetworkSpecifier(specifier.build())
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                if (wifiManager.connectionInfo.ssid == "\"$ssid\"" && wifiManager.connectionInfo.supplicantState == SupplicantState.COMPLETED) {
                    connectivityManager.bindProcessToNetwork(network)
                    Log.d(
                        TAG,
                        "onAvailable: network=$network ip=${getGateWayIp(wifiManager)} wifiInfo=${wifiManager.connectionInfo}"
                    )
                    updateNotification("Connected")
                    callback.invoke(getGateWayIp(wifiManager), network, wifiManager.connectionInfo)
                }
            }

            override fun onUnavailable() {
                Log.d(TAG, "onUnAvailable: ")
                updateNotification("Disconnected")
                stopSelf()
            }
        }
        connectivityManager.requestNetwork(request, networkCallback)
    }

    private fun getGateWayIp(wifiManager: WifiManager): String {
        val addressInt = wifiManager.dhcpInfo.gateway
        return "%d.%d.%d.%d".format(
            null,
            addressInt and 0xff,
            addressInt shr 8 and 0xff,
            addressInt shr 16 and 0xff,
            addressInt shr 24 and 0xff
        )
    }

    private fun checkBattery() {
//        val connectionBatteryLimit = preferences.getInt("connection_battery_limit", 0)
//        val connectInPowerSaveMode = preferences.getBoolean("connect_in_power_save_mode", false)
//
//        var connectionRejectionReason = 0
//
//        if (!connectInPowerSaveMode && getSystemService(PowerManager::class.java)?.isPowerSaveMode == true) {
//            connectionRejectionReason = connectionRejectionReason or AAWirelessClientService.POWER_SAVE_MODE
//        }
//
//        if (connectionBatteryLimit > 0
//            && (getSystemService(BatteryManager::class.java)?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
//                ?: 100) < connectionBatteryLimit
//        ) {
//            connectionRejectionReason = connectionRejectionReason or AAWirelessClientService.INSUFFICIENT_BATTERY
//        }
    }
}