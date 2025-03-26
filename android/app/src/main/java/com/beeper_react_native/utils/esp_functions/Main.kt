package com.beeper_react_native.utils.esp_functions

import android.app.Notification
import android.service.notification.StatusBarNotification
import android.util.Log

private val TAG = "MainEspFunctions"

fun sendDataToEsp(data : StatusBarNotification) {
    val extras = data.notification.extras

    // Extract common notification fields
    val appName = data.packageName
    val title = extras.getString(Notification.EXTRA_TITLE) ?: "No title"
    val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: "No text"
    val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
    val summaryText = extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString()

    // Build a meaningful string with the notification data
    val notificationData = """
        App: $appName
        Title: $title
        Text: $text
        ${if (subText != null) "SubText: $subText" else ""}
        ${if (summaryText != null) "Summary: $summaryText" else ""}
        Post time: ${data.postTime}
    """.trimIndent()

    Log.d(TAG, "sendDataToEsp: $notificationData")
}