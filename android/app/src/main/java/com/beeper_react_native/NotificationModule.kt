package com.beeper_react_native

import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise

class NotificationModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    override fun getName() = "NotificationModule"

    @ReactMethod
    fun requestPermissions() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        reactApplicationContext.startActivity(intent)
    }

    fun isNotificationServiceEnabled(): Boolean {
        val cn = ComponentName(reactApplicationContext, NotificationService::class.java)
        val flat = Settings.Secure.getString(
            reactApplicationContext.contentResolver,
            "enabled_notification_listeners"
        )
        return flat != null && flat.contains(cn.flattenToString())
    }

    @ReactMethod
    fun checkNotificationServiceStatus(promise: Promise) {
        try {
            val isEnabled = isNotificationServiceEnabled()
            promise.resolve(isEnabled)
        } catch (e: Exception) {
            promise.reject("ERROR", e.message)
        }
    }

    @ReactMethod
    fun openNotificationSettings() {
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        reactApplicationContext.startActivity(intent)
    }
}