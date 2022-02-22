package com.salto.claysdkdemo.application

import com.salto.claysdkdemo.models.Device
import com.salto.claysdkdemo.models.GuestDigitalKey
import com.saltosystems.justinmobile.sdk.model.MobileKey
import net.openid.appauth.AuthState

interface ISharedPrefsUtil {

    var device: Device?

    fun readAuthState(): AuthState?

    fun writeAuthState(authState: AuthState?)

    fun deleteAuthState()

    fun readAuthStateFor(accessCode: String): AuthState?

    fun writeAuthStateFor(accessCode: String, authState: AuthState?)

    fun deleteAuthStateFor(accessCode: String)

    fun getGuestDigitalKeys(): MutableList<GuestDigitalKey>

    fun saveGuestDigitalKey(key: GuestDigitalKey)
}
