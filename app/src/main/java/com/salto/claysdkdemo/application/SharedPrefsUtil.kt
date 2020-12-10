package com.salto.claysdkdemo.application

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.quality.claysdkdemo.BuildConfig
import com.salto.claysdkdemo.models.Device
import net.openid.appauth.AuthState
import java.lang.Exception

private const val STRING_PLACE_HOLDER = "%s.%s"
private const val KEY = BuildConfig.APPLICATION_ID

private const val AUTH_STATE = "auth_state"
private const val DEVICE = "device"

class SharedPrefsUtil(context: Context) : ISharedPrefsUtil {

    private val gson = Gson()

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)

    private val authStateKey: String = String.format(STRING_PLACE_HOLDER, KEY, AUTH_STATE)
    private val deviceKey: String = String.format(STRING_PLACE_HOLDER, KEY, DEVICE)

    override var device: Device?
        get()  {
            return sharedPreferences.getString(deviceKey, null)?.let {
                gson.fromJson(it, Device::class.java)
            }
        }
        set(value) {
            value?.let {
                sharedPreferences.edit().putString(deviceKey, gson.toJson(value)).apply()
            } ?: sharedPreferences.edit().remove(deviceKey).apply()

        }

    override fun readAuthState(): AuthState? {
        return sharedPreferences.getString(authStateKey, null)?.let {
            try {
                AuthState.jsonDeserialize(it)
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun writeAuthState(authState: AuthState?) {
        sharedPreferences.edit().putString(authStateKey, authState?.jsonSerializeString()).apply()
    }

    override fun deleteAuthState() {
        sharedPreferences.edit().remove(authStateKey).apply()
    }
}
