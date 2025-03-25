package com.beeper_react_native

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.facebook.react.modules.core.DeviceEventManagerModule
import android.content.pm.ServiceInfo
import android.util.Log

class NotificationService : NotificationListenerService() {
    private val channelId = "notification_channel"
    private val channelName = "Notification Service Channel"
    private val NOTIFICATION_ID = 1
    private val TAG = "NotificationService" // Tag para los logs

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Servicio onCreate iniciado")
        createNotificationChannel()
        Log.d(TAG, "Servicio inicializado completamente")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        Log.d(TAG, "Notificación recibida de: ${sbn.packageName}")

        val title = sbn.notification.extras.getCharSequence("android.title")?.toString() ?: ""
        val text = sbn.notification.extras.getCharSequence("android.text")?.toString() ?: ""

        Log.d(TAG, "Contenido: Título='$title', Texto='$text'")

        val notificationData = NotificationData(
            sbn.packageName ?: "",
            title,
            text
        )

        try {
            // Enviar a React Native
            val reactContext = (applicationContext as MainApplication).reactNativeHost.reactInstanceManager.currentReactContext
            if (reactContext != null) {
                reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                    ?.emit("onNotificationReceived", mapOf(
                        "appName" to notificationData.appName,
                        "title" to notificationData.title,
                        "text" to notificationData.text
                    ))
                Log.d(TAG, "Notificación enviada a React Native")
            } else {
                Log.e(TAG, "Error: reactContext es null, no se pudo enviar la notificación a React Native")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al procesar notificación: ${e.message}", e)
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Listener conectado - Servicio de notificaciones activo")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "Listener desconectado - Servicio de notificaciones inactivo")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        Log.d(TAG, "Creando canal de notificaciones: $channelId")
        try {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            Log.d(TAG, "Canal de notificaciones creado exitosamente")
            showForegroundNotification()
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear canal de notificaciones: ${e.message}", e)
        }
    }

    private fun showForegroundNotification() {
        Log.d(TAG, "Mostrando notificación de primer plano")
        try {
            val intent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle("Beeper Service")
                .setContentText("Capturando notificaciones")
                .setContentIntent(pendingIntent)
                .setSmallIcon(android.R.drawable.ic_notification_overlay)
                .build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Log.d(TAG, "Iniciando servicio en primer plano con TYPE_DATA_SYNC (Android 10+)")
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
            } else {
                Log.d(TAG, "Iniciando servicio en primer plano (Android pre-10)")
                startForeground(NOTIFICATION_ID, notification)
            }
            Log.d(TAG, "Notificación de primer plano mostrada exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al mostrar notificación de primer plano: ${e.message}", e)
        }
    }
}