package com.salto.claysdkdemo.services

import com.salto.claysdkdemo.listeners.IAuthenticationServiceListener

interface IOIDAuthenticator {

    fun setAuthenticationDelegate(listener: IAuthenticationServiceListener?)

    fun execute(callback: AuthCallback?)
}