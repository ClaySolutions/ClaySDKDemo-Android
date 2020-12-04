package com.salto.claysdkdemo.login.oid

import net.openid.appauth.AuthorizationException

class AuthOIDError(private val exception: AuthorizationException) {

    override fun toString(): String {
        return when {
            exception.errorDescription != null -> exception.errorDescription!!
            exception.error != null -> exception.error!!
            else -> exception.toString()
        }
    }

}
