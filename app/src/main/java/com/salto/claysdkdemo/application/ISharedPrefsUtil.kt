package com.salto.claysdkdemo.application

import com.salto.claysdkdemo.models.Device
import net.openid.appauth.AuthState

interface ISharedPrefsUtil {

    var device: Device?

    fun readAuthState(): AuthState?

    fun writeAuthState(authState: AuthState?)

    fun deleteAuthState()
}
