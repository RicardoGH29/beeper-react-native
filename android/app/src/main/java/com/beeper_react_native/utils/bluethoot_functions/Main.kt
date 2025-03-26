package com.beeper_react_native.utils.bluethoot_functions

import android.Manifest
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.beeper_react_native.MainActivity
import com.beeper_react_native.R
import java.time.LocalDateTime
import java.util.UUID

const val REQUEST_BLUETOOTH_CONNECT_PERMISSION = 3
val resultsBluethootDevices = mutableListOf<String>()
private val TAG = "BluetoothFunctions"
var selectedDeviceAddress: String? = null
var bluetoothGatt: BluetoothGatt? = null

var isGattConnected = false

fun extractMacAddress(deviceInfo: String): String {
    val startIndex = deviceInfo.lastIndexOf("(") + 1
    val endIndex = deviceInfo.lastIndexOf(")")
    return if (startIndex > 0 && endIndex > startIndex) {
        deviceInfo.substring(startIndex, endIndex)
    } else {
        ""
    }
}


fun sendDataToDevice(context: Context, data: String): View.OnClickListener {
    Log.d(TAG, "sendDataToDevice: send data")
    return View.OnClickListener {
        Log.d(TAG, "sendDataToDevice: $bluetoothGatt")
        if (bluetoothGatt == null) {
            Toast.makeText(context, "No hay conexión activa", Toast.LENGTH_SHORT).show()
            return@OnClickListener
        }

        try {
            // UUID debe coincidir con el configurado en el ESP32
            val serviceUUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
            val charUUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")

            val service = bluetoothGatt?.getService(serviceUUID)
            if (service == null) {
                Toast.makeText(context, "Servicio no encontrado", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            val characteristic = service.getCharacteristic(charUUID)
            if (characteristic == null) {
                Toast.makeText(context, "Característica no encontrada", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            characteristic.setValue(data)

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED) {
                val success = bluetoothGatt?.writeCharacteristic(characteristic) ?: false
                if (success) {
                    Toast.makeText(context, "Datos enviados correctamente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Error al enviar datos", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Permiso BLUETOOTH_CONNECT necesario", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error enviando datos: ${e.message}")
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

fun sendDataDirectly(context: Context, data: String): Boolean {
    if (bluetoothGatt == null) {
        Log.d(TAG, "No active connection to send data")
        return false
    }

    try {
        // UUID must match ESP32 configuration
        val serviceUUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
        val charUUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")

        val service = bluetoothGatt?.getService(serviceUUID)
        if (service == null) {
            Log.d(TAG, "Service not found")
            return false
        }

        val characteristic = service.getCharacteristic(charUUID)
        if (characteristic == null) {
            Log.d(TAG, "Characteristic not found")
            return false
        }

        // Set the characteristic value (data to be written)
        characteristic.setValue(data)

        // Set write type to get confirmation
        characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
            == PackageManager.PERMISSION_GRANTED) {

            // Register for write callback in the gattCallback if not already done
            // The actual success will be reported in onCharacteristicWrite

            Log.d(TAG, "Attempting to send data: $data")
            return bluetoothGatt?.writeCharacteristic(characteristic) ?: false
        } else {
            Log.d(TAG, "BLUETOOTH_CONNECT permission required")
            return false
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error sending data: ${e.message}")
        return false
    }
}
