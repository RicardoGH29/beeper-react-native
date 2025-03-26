package com.beeper_react_native.utils

import android.content.Context
import androidx.core.content.edit


class AppsSelectedManager private constructor(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

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