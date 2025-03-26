package com.beeper_react_native

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.beeper_react_native.utils.AppsSelectedManager
import com.beeper_react_native.utils.bluethoot_functions.sendDataToDevice

data class NotificationData(
    val appName: String,
    val title: String,
    val text: String
)


class NotificationService : NotificationListenerService() {


    private val TAG = "NotificationService"

    private val channelId = "notification_channel"
    private val channelName = "Notification Service Channel"

    val selectedManager by lazy {
        AppsSelectedManager.getInstance(this)
    }



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val appName = sbn.packageName ?: ""
        val title = sbn.notification.extras.getCharSequence("android.title")?.toString() ?: ""
        val text = sbn.notification.extras.getCharSequence("android.text")?.toString() ?: ""

        val selectedApps = selectedManager.getAllSelectedApps()

        Log.d(TAG, "onNotificationPosted: $selectedApps")

//        if (selectedApps.contains(appName)) {
            val notificationData = NotificationData(appName, title, text)

            // Serializar con formato: <typeMessage>|<app>|<title>|<text>
            val dataToSend = "notification|${notificationData.appName}|${notificationData.title}|${notificationData.text}"

            // Enviar datos al dispositivo ESP
            val context = this
            sendDataToDevice(context, dataToSend).onClick(null)

    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        Log.d(TAG, "createNotificationChannel: Se creo ")
        val channel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
        showForegroundNotification()
    }

    private fun showForegroundNotification() {

        Log.d(TAG, "showForegroundNotification: El servicio está activo")

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Beeper Service")
            .setContentText("Se estan capturando notificaciones")
            .setContentIntent(pendingIntent) // Esto captura el clic en la notificación
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .build()

        startForeground(1, notification)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Aquí puedes manejar cuando una notificación se elimina
        Log.d(TAG, "onNotificationRemoved: Se elimino la notificación")
        val appName = sbn.packageName
        Log.d(TAG, "onNotificationRemoved: $appName")
        if (appName == "com.beeper_react_native") {
            showForegroundNotification()
        }
    }
}
