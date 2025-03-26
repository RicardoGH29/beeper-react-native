package com.beeper_react_native.utils.esp_functions

import android.app.AlertDialog
import android.content.Context
import android.net.wifi.WifiManager
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import com.beeper_react_native.utils.bluethoot_functions.sendDataToDevice

fun getConnectedWifiSSID(context: Context): String {
    val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val wifiInfo = wifiManager.connectionInfo

    var ssid = wifiInfo.ssid
    if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
        ssid = ssid.substring(1, ssid.length - 1)
    }

    return if (ssid == "<unknown ssid>") "No conectado" else ssid
}

fun sendWifiCredentialsToESP32(context: Context) {
    val ssid = getConnectedWifiSSID(context)

    val builder = AlertDialog.Builder(context)
    builder.setTitle("Enviar credenciales WiFi")
    builder.setMessage("SSID: $ssid\nIntroduce la contraseña:")

    val input = EditText(context)
    input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
    builder.setView(input)

    builder.setPositiveButton("Enviar") { _, _ ->
        val password = input.text.toString()
        val dataToSend = "wifi|$ssid|$password"
        sendDataToDevice(context, dataToSend).onClick(null)
    }

    builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
    builder.show()
}