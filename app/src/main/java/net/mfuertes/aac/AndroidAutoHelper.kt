package net.mfuertes.aac

import android.content.Context
import android.content.Intent
import android.net.Network
import android.net.wifi.WifiInfo

class AndroidAutoHelper {
 companion object {

     const val PACKAGE_NAME_ANDROID_AUTO_WIRELESS = "com.google.android.projection.gearhead"
     const val CLASS_NAME_ANDROID_AUTO_WIRELESS =
         "com.google.android.apps.auto.wireless.setup.service.impl.WirelessStartupActivity"
     const val PARAM_HOST_ADDRESS_EXTRA_NAME = "PARAM_HOST_ADDRESS"
     const val PARAM_SERVICE_PORT_EXTRA_NAME = "PARAM_SERVICE_PORT"
     const val PARAM_SERVICE_WIFI_NETWORK_EXTRA_NAME = "PARAM_SERVICE_WIFI_NETWORK"
     const val PARAM_WIFI_INFO_EXTRA_NAME = "wifi_info"
     fun connectAAWireless(context: Context, address: String, network: Network?, wifiInfo: WifiInfo?) {
         val intent = Intent()
             .setClassName(PACKAGE_NAME_ANDROID_AUTO_WIRELESS, CLASS_NAME_ANDROID_AUTO_WIRELESS)
             .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
             .putExtra(PARAM_HOST_ADDRESS_EXTRA_NAME, address)
             .putExtra(PARAM_SERVICE_PORT_EXTRA_NAME, 5288)
             .putExtra(PARAM_SERVICE_WIFI_NETWORK_EXTRA_NAME, network)
             .putExtra(PARAM_WIFI_INFO_EXTRA_NAME, wifiInfo)

         context.startActivity(intent)
     }

     fun connectLocalhostAAWireless(context: Context) {
         val intent = Intent()
             .setClassName(PACKAGE_NAME_ANDROID_AUTO_WIRELESS, CLASS_NAME_ANDROID_AUTO_WIRELESS)
             .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
             .putExtra(PARAM_HOST_ADDRESS_EXTRA_NAME, "127.0.0.1")
             .putExtra(PARAM_SERVICE_PORT_EXTRA_NAME, 5288)

         context.startActivity(intent)
     }
 }
}