package net.mfuertes.aac

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.content.res.AppCompatResources
import androidx.preference.*
import net.mfuertes.aac.helpers.BluetoothHandler
import net.mfuertes.aac.helpers.WifiClientHandler
import net.mfuertes.aac.receivers.BluetoothReceiver
import net.mfuertes.aac.services.AAWirelessClientService

class SettingsFragment : PreferenceFragmentCompat() {
    private var mBluetoothHandler: BluetoothHandler? = null
    private var mWifiClientHandler: WifiClientHandler? = null

    private var mErrorIcon: Drawable? = null
    private var mDoneIcon: Drawable? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        mBluetoothHandler = BluetoothHandler(context, this)
        mWifiClientHandler = WifiClientHandler(context, this)
    }

    override fun onDetach() {
        super.onDetach()

        mBluetoothHandler = null
        mWifiClientHandler = null
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        findPreference<EditTextPreference>("gateway_wifi_password")?.apply {
            setSummaryProvider {
                val length = (it as? EditTextPreference)?.text?.length ?: 0

                if (length > 0) "*".repeat(length) else "Not set"
            }
        }
        findPreference<Preference>("start_manual")?.apply {
            setOnPreferenceClickListener {
                context.startForegroundService(Intent(context, AAWirelessClientService::class.java))
                return@setOnPreferenceClickListener true
            }
        }
        findPreference<Preference>("gateway_bt_mac")?.also { setBluetoothDevices(it) }

        findPreference<Preference>("bluetooth_permissions")?.apply {
            setOnPreferenceClickListener {
                mBluetoothHandler?.requestConnectPermissions {
                    updateSettingsState(context)
                }
                true
            }
        }
        findPreference<Preference>("location_permissions")?.apply {
            setOnPreferenceClickListener {
                mWifiClientHandler?.apply {
                    if (!hasLocationPermissions()) {
                        requestLocationPermissions {
                            updateSettingsState(context)
                        }
                    }
                    else if (!hasBackgroundLocationPermission()) {
                        requestBackgroundLocationPermissions {
                            updateSettingsState(context)
                        }
                    }
                }
                true
            }
        }

        context?.also {
            updateSettingsState(it)
        }
    }

    override fun onResume() {
        super.onResume()

        context?.also {
            updateSettingsState(it)
        }
    }

    private fun updateSettingsState(context: Context) {
        if (mErrorIcon == null) {
            mErrorIcon = AppCompatResources.getDrawable(context, R.drawable.ic_baseline_error_outline_24)
        }

        if (mDoneIcon == null) {
            mDoneIcon = AppCompatResources.getDrawable(context, R.drawable.ic_baseline_done_24)
        }

        findPreference<Preference>("write_settings_permission")?.apply {
            isEnabled = !Settings.System.canWrite(context)
            summary = if (isEnabled) "Required to automatically connect to Wifi" else "Already granted"
            icon = if (isEnabled) mErrorIcon else mDoneIcon
        }

        findPreference<Preference>("system_alert_window_permission")?.apply {
            isEnabled = !Settings.canDrawOverlays(context)
            summary = if (isEnabled) "Required to start Android Auto" else "Already granted"
            icon = if (isEnabled) mErrorIcon else mDoneIcon
        }

        findPreference<Preference>("bluetooth_permissions")?.apply {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) {
                isVisible = false
                return@apply
            }

            isEnabled = !(mBluetoothHandler?.hasConnectPermissions() ?: false)
            summary = if (isEnabled) "Required to connect with bluetooth" else "Already granted"
            icon = if (isEnabled) mErrorIcon else mDoneIcon
        }

        findPreference<Preference>("location_permissions")?.apply {
            val hasLocationPermissions = mWifiClientHandler?.hasLocationPermissions() ?: false
            val hasBackgroundLocationPermissions = mWifiClientHandler?.hasBackgroundLocationPermission() ?: false
            isEnabled = !(hasLocationPermissions && hasBackgroundLocationPermissions)
            summary = if (!hasLocationPermissions)
                "Precise location permission is required to get connected wifi information"
            else if (!hasBackgroundLocationPermissions)
                "Please also grant background location permission by selecting \"Allow all the time\""
            else
                "Already granted"
            icon = if (isEnabled) mErrorIcon else if (hasLocationPermissions && hasBackgroundLocationPermissions) mDoneIcon else null
        }
    }

    private fun setBluetoothDevices(preference: Preference) {
        mBluetoothHandler?.also { bluetoothHandler ->
            val devices = bluetoothHandler.getBondedDevices()

            val entries = devices.map { "${it.name} (${it.address})" }.toTypedArray()
            val entryValues = devices.map { it.address }.toTypedArray()

            when (preference) {
                is ListPreference -> {
                    preference.entries = entries
                    preference.entryValues = entryValues
                }
                is MultiSelectListPreference -> {
                    preference.entries = entries
                    preference.entryValues = entryValues
                }
                else -> {
                    // Cannot handle this preference
                }
            }
        }
    }
}