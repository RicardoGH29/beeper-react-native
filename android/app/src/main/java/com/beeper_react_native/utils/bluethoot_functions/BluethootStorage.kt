package com.beeper_react_native.utils.bluethoot_functions

import android.content.Context
import android.content.SharedPreferences

class BluetoothStorage(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("BluetoothDevices", Context.MODE_PRIVATE)

    private val deviceKey = "paired_devices"

    fun addDevice(deviceAddress: String) {
        val devices = getDevices().toMutableSet()
        devices.add(deviceAddress)
        sharedPreferences.edit().putStringSet(deviceKey, devices).apply()
    }

    fun removeDevice(deviceAddress: String) {
        val devices = getDevices().toMutableSet()
        devices.remove(deviceAddress)
        sharedPreferences.edit().putStringSet(deviceKey, devices).apply()
    }

    fun getDevices(): Set<String> {
        return sharedPreferences.getStringSet(deviceKey, emptySet()) ?: emptySet()
    }

    fun isDevicePaired(deviceAddress: String): Boolean {
        return getDevices().contains(deviceAddress)
    }
}