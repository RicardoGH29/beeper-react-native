package com.beeper_react_native.utils

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.RequiresPermission
import com.beeper_react_native.utils.bluethoot_functions.bluetoothGatt
import com.beeper_react_native.utils.bluethoot_functions.isGattConnected
import android.Manifest
import java.util.UUID

object BluetoothConnectionManager {
    private const val PREF_NAME = "BluetoothConnectionPrefs"
    private const val KEY_CONNECTED_DEVICE_ID = "connectedDeviceId"
    private const val KEY_CONNECTED_DEVICE_NAME = "connectedDeviceName"
    private const val KEY_IS_CONNECTED = "isConnected"
    private val TAG = "BluetoothConnectionManager"

    // Añadir al BluetoothConnectionManager.kt
    fun connectToDevice(context: Context, deviceAddress: String): Boolean {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        // Verificar permisos
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) !=
            PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "No hay permiso BLUETOOTH_CONNECT")
            return false
        }

        // Obtener dispositivo por dirección MAC
        val device = bluetoothAdapter.getRemoteDevice(deviceAddress)

        // Definir callback para la conexión GATT
        val gattCallback = object : BluetoothGattCallback() {
            @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d(TAG, "Conectado a GATT server")
                    isGattConnected = true
                    bluetoothGatt = gatt

                    // Guardar información del dispositivo conectado
                    saveConnectedDevice(context, deviceAddress, device.name ?: "Dispositivo desconocido")

                    // Descubrir servicios
                    gatt.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d(TAG, "Desconectado de GATT server")
                    isGattConnected = false
                    bluetoothGatt = null
                    clearConnectedDevice(context)
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d(TAG, "Servicios descubiertos")
                    // Aquí puedes trabajar con los servicios descubiertos
                }
            }
        }

        // Conectar al dispositivo
        bluetoothGatt = device.connectGatt(context, false, gattCallback)

        return true
    }
    /**
     * Desconecta el dispositivo BluetoothGatt actual si existe una conexión.
     *
     * @param context El contexto de la aplicación
     * @return true si se inició la desconexión, false si no había conexión activa o faltaban permisos
     */
    fun disconnectDevice(context: Context): Boolean {
        // Verificar que el dispositivo está conectado
        if (bluetoothGatt == null) {
            Log.d(TAG, "disconnectDevice: No hay dispositivo conectado")
            return false
        }

        // Verificar permisos
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) !=
            PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "No hay permiso BLUETOOTH_CONNECT")
            return false
        }

        try {
            Log.d(TAG, "Desconectando dispositivo GATT...")
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
            bluetoothGatt = null
            isGattConnected = false

            // Actualizar las preferencias
            clearConnectedDevice(context)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error al desconectar: ${e.message}")
            return false
        }
    }

    fun saveConnectedDevice(context: Context, deviceId: String, deviceName: String) {
        Log.d(TAG, "saveConnectedDevice: $deviceName $deviceId")
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_CONNECTED_DEVICE_ID, deviceId)
            putString(KEY_CONNECTED_DEVICE_NAME, deviceName)
            putBoolean(KEY_IS_CONNECTED, true)
        }.apply()
    }

    fun clearConnectedDevice(context: Context) {
        disconnectDevice(context)
    }

    fun getConnectedDevice(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return if (prefs.getBoolean(KEY_IS_CONNECTED, false)) {
            prefs.getString(KEY_CONNECTED_DEVICE_NAME, null)
        } else null
    }

    fun sendDataToDevice(context: Context, data: String) {
        Log.d(TAG, "sendDataToDevice: $data")
        // Check if we have an active Bluetooth connection
        if (bluetoothGatt == null || !isGattConnected) {
            Log.e(TAG, "sendDataToDevice: No active Bluetooth connection")
            return
        }

        // Verify permissions
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) !=
            PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "sendDataToDevice: Missing BLUETOOTH_CONNECT permission")
            return
        }

        try {
            // Convert string to bytes
            val dataBytes = data.toByteArray(Charsets.UTF_8)
            Log.d(TAG, "Sending data: $data")

            // These UUIDs need to be replaced with your device's specific UUIDs
            val serviceUUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
            val charUUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")

            val service = bluetoothGatt?.getService(serviceUUID)
            if (service == null) {
                Log.e(TAG, "Service not found")
                return
            }

            val characteristic = service.getCharacteristic(charUUID)
            if (characteristic == null) {
                Log.e(TAG, "Characteristic not found")
                return
            }

            // Write data to the characteristic
            characteristic.value = dataBytes
            val success = bluetoothGatt?.writeCharacteristic(characteristic) ?: false

            if (success) {
                Log.d(TAG, "Data sent successfully")
            } else {
                Log.e(TAG, "Failed to send data")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending data: ${e.message}", e)
        }
    }

    fun getConnectedDeviceId(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return if (prefs.getBoolean(KEY_IS_CONNECTED, false)) {
            prefs.getString(KEY_CONNECTED_DEVICE_ID, null)
        } else null
    }

    fun isConnected(context: Context): Boolean {
        return isGattConnected && bluetoothGatt != null
    }
}
