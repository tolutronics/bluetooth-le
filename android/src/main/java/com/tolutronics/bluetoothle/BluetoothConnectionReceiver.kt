package com.tolutronics.bluetoothle

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.getcapacitor.Logger

/**
 * BroadcastReceiver that listens for Bluetooth connection changes even when app is backgrounded.
 * This captures Bluetooth Classic connections (like car audio systems) that aren't detected by BLE.
 */
class BluetoothConnectionReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "BluetoothConnectionRcvr"
        private var pluginInstance: BluetoothLe? = null

        fun setPluginInstance(plugin: BluetoothLe?) {
            pluginInstance = plugin
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        Logger.debug(TAG, "Received broadcast: $action")

        when (action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                handleDeviceConnected(intent)
            }
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                handleDeviceDisconnected(intent)
            }
            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                handleBluetoothStateChanged(intent)
            }
        }
    }

    private fun handleDeviceConnected(intent: Intent) {
        val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        if (device == null) {
            Logger.warn(TAG, "Device connected but no device info available")
            return
        }

        val deviceName = try {
            device.name ?: "(no name)"
        } catch (e: SecurityException) {
            Logger.warn(TAG, "Missing BLUETOOTH_CONNECT permission")
            "(no name)"
        }

        val deviceAddress = device.address

        Logger.info(TAG, "Bluetooth device connected: $deviceName [$deviceAddress]")

        // Notify the plugin
        pluginInstance?.notifyBluetoothConnectionChange(
            connected = true,
            deviceId = deviceAddress,
            deviceName = deviceName
        )
    }

    private fun handleDeviceDisconnected(intent: Intent) {
        val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        if (device == null) {
            Logger.warn(TAG, "Device disconnected but no device info available")
            return
        }

        val deviceName = try {
            device.name ?: "(no name)"
        } catch (e: SecurityException) {
            Logger.warn(TAG, "Missing BLUETOOTH_CONNECT permission")
            "(no name)"
        }

        val deviceAddress = device.address

        Logger.info(TAG, "Bluetooth device disconnected: $deviceName [$deviceAddress]")

        // Notify the plugin
        pluginInstance?.notifyBluetoothConnectionChange(
            connected = false,
            deviceId = deviceAddress,
            deviceName = deviceName
        )
    }

    private fun handleBluetoothStateChanged(intent: Intent) {
        val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
        when (state) {
            BluetoothAdapter.STATE_OFF -> {
                Logger.info(TAG, "Bluetooth turned off")
            }
            BluetoothAdapter.STATE_ON -> {
                Logger.info(TAG, "Bluetooth turned on")
            }
        }
    }
}
