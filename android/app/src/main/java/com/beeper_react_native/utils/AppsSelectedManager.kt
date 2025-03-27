package com.beeper_react_native.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Base64
import android.util.Log
import androidx.core.content.edit
import androidx.core.graphics.createBitmap
import java.io.ByteArrayOutputStream

class AppsSelectedManager private constructor(private val context: Context) {

    private val TAG = "AppsSelectedManager"
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getAllApps(): Set<String> {
        return prefs.all.keys
    }

    fun saveSelectedApp(packageName: String, isSelected: Boolean) {
        prefs.edit {
            putBoolean(packageName, isSelected)
        }
    }

    fun isAppSelected(packageName: String): Boolean {
        return prefs.getBoolean(packageName, false)
    }

    fun getAllSelectedApps(): Set<String> {
        return prefs.all.filter { it.value == true }.keys.toSet()
    }

    private fun drawableToBase64(drawable: Drawable): String {
        var bitmap: Bitmap? = null

        if (drawable is BitmapDrawable) {
            bitmap = drawable.bitmap
        } else {
            bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
        }

        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    /**
     * Gets all installed applications on the device.
     *
     * @param includeSystemApps Whether to include system applications in the result
     * @return List of installed app information containing package name, display name, and whether it's a system app
     */
    fun getInstalledApps(includeSystemApps: Boolean = false): List<AppInfo> {
        val packageManager = context.packageManager
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        Log.d(TAG, "getInstalledApps: Get All apps")

        val allApps = installedApps
            .filter { appInfo ->
                includeSystemApps || (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0
            }
            .map { appInfo ->
                AppInfo(
                    packageName = appInfo.packageName,
                    appName = packageManager.getApplicationLabel(appInfo).toString(),
                    isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                    image = drawableToBase64(packageManager.getApplicationIcon(appInfo))
                )
            }
        Log.d(TAG, "getInstalledApps: $allApps")
        return allApps
    }

    /**
     * Data class representing information about an installed application.
     */
    data class AppInfo(
        val packageName: String,
        val appName: String,
        val isSystemApp: Boolean,
        val image: String
    )

    companion object {
        private const val PREFS_NAME = "selected_apps_prefs"
        private var INSTANCE: AppsSelectedManager? = null

        fun getInstance(context: Context): AppsSelectedManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppsSelectedManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}