package com.beeper_react_native

import android.content.Context
import android.util.Log
import com.beeper_react_native.utils.BluetoothConnectionManager
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule

class BluetoothConnectionModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    init {
        // Initialize the React context in BluetoothConnectionManager
        BluetoothConnectionManager.setReactContext(reactContext)
    }

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
    fun getConnectedDevice(promise: Promise) {
        val device = BluetoothConnectionManager.getConnectedDevice(reactApplicationContext)
        promise.resolve(device ?: "")
    }

    @ReactMethod
    fun sendDataToDevice(data: String) {
        Log.d("Test", "sendDataToDevice: $data")
        BluetoothConnectionManager.sendDataToDevice(reactApplicationContext, data)
    }

    @ReactMethod
    fun isConnected(promise: Promise) {
        val connected = BluetoothConnectionManager.isConnected(reactApplicationContext)
        promise.resolve(connected)
    }

    @ReactMethod
    fun connectToDevice(deviceAddress: String, promise: Promise) {
        val result = BluetoothConnectionManager.connectToDevice(reactApplicationContext, deviceAddress)
        promise.resolve(result)
    }

    // Required methods for React Native event system
    @ReactMethod
    fun addListener(eventName: String) {
        // Required for RN built-in Event Emitter
    }

    @ReactMethod
    fun removeListeners(count: Int) {
        // Required for RN built-in Event Emitter
    }
}