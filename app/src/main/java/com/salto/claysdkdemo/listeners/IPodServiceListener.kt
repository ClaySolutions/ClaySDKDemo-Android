package com.salto.claysdkdemo.listeners

import com.salto.claysdkdemo.models.Device

interface IPodServiceListener: IAuthenticationServiceListener {

    fun onPodGuestRegistered()

    fun didReceivePodGuestRegisterError(error: String?)
}