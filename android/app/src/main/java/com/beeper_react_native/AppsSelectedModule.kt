package com.beeper_react_native

import com.beeper_react_native.utils.AppsSelectedManager
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule

class AppsSelectedModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    private val appsSelectedManager: AppsSelectedManager by lazy {
        AppsSelectedManager.getInstance(reactContext)
    }

    override fun getName(): String {
        return "AppsSelectedModule"
    }

    @ReactMethod
    fun saveSelectedApp(packageName: String, isSelected: Boolean) {
        appsSelectedManager.saveSelectedApp(packageName, isSelected)
    }

    @ReactMethod
    fun getAllApps(systemApps: Boolean, promise: Promise) {
        val apps = appsSelectedManager.getInstalledApps(systemApps)
        val array = Arguments.createArray()
        apps.forEach { app ->
            val map = Arguments.createMap()
            map.putString("packageName", app.packageName)
            map.putString("appName", app.appName)
            map.putBoolean("isSystemApp", app.isSystemApp)
            map.putString("image", app.image.toString())
            array.pushMap(map)
        }
        promise.resolve(array)
    }

    @ReactMethod
    fun isAppSelected(packageName: String, promise: Promise) {
        try {
            val isSelected = appsSelectedManager.isAppSelected(packageName)
            promise.resolve(isSelected)
        } catch (e: Exception) {
            promise.reject("ERROR", e.message)
        }
    }

    @ReactMethod
    fun getAllSelectedApps(promise: Promise) {
        try {
            val selectedApps = appsSelectedManager.getAllSelectedApps()
            val array = Arguments.createArray()
            selectedApps.forEach { array.pushString(it) }
            promise.resolve(array)
        } catch (e: Exception) {
            promise.reject("ERROR", e.message)
        }
    }
} 