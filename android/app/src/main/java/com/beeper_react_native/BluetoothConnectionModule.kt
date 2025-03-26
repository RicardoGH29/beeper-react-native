package com.beeper_react_native

import android.content.Context
import android.util.Log
import com.beeper_react_native.utils.BluetoothConnectionManager
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class BluetoothConnectionModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    override fun getName(): String {
        return "BluetoothConnectionModule"
    }

    @ReactMethod
    fun saveConnectedDevice(deviceId: String, deviceName: String) {
        BluetoothConnectionManager.saveConnectedDevice(reactApplicationContext, deviceId, deviceName ?: "Unknown")
    }

    @ReactMethod
    fun clearConnectedDevice() {
        BluetoothConnectionManager.clearConnectedDevice(reactApplicationContext)
    }

    @ReactMethod
    fun getConnectedDevice(): String {
        return BluetoothConnectionManager.getConnectedDevice(reactApplicationContext).toString()
    }

    @ReactMethod
    fun sendDataToDevice(data: String) {
        Log.d("Test", "sendDataToDevice: $data")
        return BluetoothConnectionManager.sendDataToDevice(reactApplicationContext, data)
    }

    @ReactMethod
    fun isConnected(): Boolean {
        return BluetoothConnectionManager.isConnected(reactApplicationContext)
    }

    @ReactMethod
    fun connectToDevice(deviceAddress: String): Boolean {
        return BluetoothConnectionManager.connectToDevice(reactApplicationContext, deviceAddress)
    }
}
