package com.salto.claysdkdemo.services

import com.salto.claysdkdemo.listeners.IDeviceServiceListener
import com.salto.claysdkdemo.listeners.IPodServiceListener

interface IPodService {

    fun setServiceListener(listener: IPodServiceListener)

    fun registerPodGuest(firstName: String, lastName: String)
}
