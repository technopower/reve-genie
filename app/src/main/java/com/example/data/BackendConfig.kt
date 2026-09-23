package com.example.data

import android.os.Build
import android.util.Log

/**
 * Centralized Backend Configuration for REVE GENIE.
 * Automatically resolves the correct server base URL depending on whether
 * the app is running on an Android Emulator or a Physical Android Phone.
 */
object BackendConfig {
    private const val TAG = "BACKEND_CONFIG"

    const val PORT = 8000
    const val EMULATOR_BASE_URL = "http://10.0.2.2:$PORT/"
    const val LAN_BASE_URL = "http://192.168.0.200:$PORT/"

    /**
     * Detects if the current device is an Android Emulator or Physical Hardware.
     * Uses null-safe operators to prevent NullPointerException in plain JVM unit tests.
     */
    fun isEmulator(): Boolean {
        val fingerprint = Build.FINGERPRINT.orEmpty()
        val model = Build.MODEL.orEmpty()
        val manufacturer = Build.MANUFACTURER.orEmpty()
        val hardware = Build.HARDWARE.orEmpty()
        val product = Build.PRODUCT.orEmpty()

        return (fingerprint.startsWith("generic")
                || fingerprint.startsWith("unknown")
                || model.contains("google_sdk")
                || model.contains("Emulator")
                || model.contains("Android SDK built for x86")
                || manufacturer.contains("Genymotion")
                || hardware.contains("goldfish")
                || hardware.contains("ranchu")
                || product.contains("sdk_gphone")
                || product.contains("google_sdk")
                || product.contains("sdk")
                || product.contains("sdk_x86")
                || product.contains("vbox86p")
                || product.contains("emulator")
                || product.contains("simulator"))
    }

    /**
     * Resolves active base URL without trailing slash.
     */
    val BASE_URL: String
        get() {
            val url = if (isEmulator()) {
                EMULATOR_BASE_URL
            } else {
                LAN_BASE_URL
            }.trimEnd('/')
            try {
                Log.d(TAG, "Translator API Base URL: $url (isEmulator=${isEmulator()})")
            } catch (_: Throwable) {
                // Ignore Log.d during plain JVM unit tests
            }
            return url
        }
}
