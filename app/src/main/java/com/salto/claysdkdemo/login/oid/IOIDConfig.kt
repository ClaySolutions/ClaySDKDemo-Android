package com.salto.claysdkdemo.login.oid

import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationRequest

interface IOIDConfig  {

    fun getLogoutRequest(authState: AuthState): String

    fun getAuthorizationRequest(authState: AuthState): AuthorizationRequest?
}