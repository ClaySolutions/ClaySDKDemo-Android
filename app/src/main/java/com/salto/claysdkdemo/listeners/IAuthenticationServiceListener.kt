package com.salto.claysdkdemo.listeners

import com.salto.claysdkdemo.models.OAuthAccessToken
import net.openid.appauth.AuthState

interface IAuthenticationServiceListener {

    fun didReceiveAuthenticationToken(token: OAuthAccessToken?) = Unit

    fun didReceiveAuthState(state: AuthState?)

    fun didReceiveAuthenticationError()

    fun didReceiveAuthenticationFailure()
}