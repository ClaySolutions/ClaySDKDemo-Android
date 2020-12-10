package com.salto.claysdkdemo.services

import com.salto.claysdkdemo.listeners.IDeviceServiceListener

interface IDeviceService {

    fun registerDevice(deviceName: String, deviceUid: String, publicKey: String)

    fun getMobileKey(userId: String)

    fun getDeviceList(deviceUUID: String)

    fun getDevice(deviceId: String)

    fun putDeviceCertificate(userId: String, publicKey: String)

    fun setMKeyActivationServiceListener(deviceServiceListener: IDeviceServiceListener)
}
