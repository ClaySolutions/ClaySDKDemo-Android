package com.salto.claysdkdemo.application

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.salto.claysdkdemo.BuildConfig
import com.salto.claysdkdemo.models.Device
import com.salto.claysdkdemo.models.GuestDigitalKey
import net.openid.appauth.AuthState
import java.lang.Exception

private const val STRING_PLACE_HOLDER = "%s.%s"
private const val KEY = BuildConfig.APPLICATION_ID

private const val AUTH_STATE = "auth_state"
private const val DEVICE = "device"
private const val GUEST_DKEYS = "guest_dkeys"

class SharedPrefsUtil(context: Context) : ISharedPrefsUtil {

    private val gson = Gson()

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)

    private val authStateKey: String = String.format(STRING_PLACE_HOLDER, KEY, AUTH_STATE)
    private val deviceKey: String = String.format(STRING_PLACE_HOLDER, KEY, DEVICE)
    private val guestDigitalKeysKey: String = String.format(STRING_PLACE_HOLDER, KEY, GUEST_DKEYS)

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

    override fun saveGuestDigitalKey(key: GuestDigitalKey) {
        val currentDigitalKeys = getGuestDigitalKeys()
        currentDigitalKeys.add(key)
        val currentDigitalKeysString = gson.toJson(currentDigitalKeys)
        sharedPreferences.edit().putString(guestDigitalKeysKey, currentDigitalKeysString).apply()
    }

    override fun getGuestDigitalKeys(): MutableList<GuestDigitalKey> {
        val json = sharedPreferences.getString(guestDigitalKeysKey, null) ?: return mutableListOf()
        val tokenType = object : TypeToken<List<GuestDigitalKey>>() {}.type
        val list = gson.fromJson<List<GuestDigitalKey>?>(json, tokenType) ?: return mutableListOf()
        return list.toMutableList()
    }
}
