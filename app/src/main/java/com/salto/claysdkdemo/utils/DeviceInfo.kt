package com.salto.claysdkdemo.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import java.util.*

object DeviceInfo {

    @JvmStatic
    val model: String
        get() {
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            return if (model.startsWith(manufacturer)) {
                capitalize(model)
            } else capitalize(manufacturer) + " " + model
        }

    @JvmStatic
    val os: String
        get() {
            val version = Build.VERSION.RELEASE
            return "Android $version"
        }

    private fun capitalize(s: String?): String {
        if (s == null || s.isEmpty()) {
            return ""
        }
        val first = s[0]
        return if (Character.isUpperCase(first)) {
            s
        } else Character.toUpperCase(first) + s.substring(1)
    }

    @SuppressLint("HardwareIds")
    @JvmStatic
    fun uniquePseudoID(context: Context): String {
        @Suppress("DEPRECATION")
        val cpuAbi: String = when (Build.VERSION.SDK_INT) {
            in Build.VERSION_CODES.BASE..Build.VERSION_CODES.LOLLIPOP -> Build.CPU_ABI
            else -> Build.SUPPORTED_ABIS[0]
        }

        val mSzDevIDShort = "35" + Build.BOARD.length % 10 + Build.BRAND.length % 10 + cpuAbi.length % 10 +
                Build.DEVICE.length % 10 + Build.MANUFACTURER.length % 10 +
                Build.MODEL.length % 10 + Build.PRODUCT.length % 10

        val androidId: String? = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

        return UUID(mSzDevIDShort.hashCode().toLong(), androidId.hashCode().toLong()).toString()
    }
}