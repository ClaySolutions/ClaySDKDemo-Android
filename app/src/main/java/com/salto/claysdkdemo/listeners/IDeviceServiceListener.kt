package com.salto.claysdkdemo.listeners

import com.salto.claysdkdemo.models.Device

interface IDeviceServiceListener: IAuthenticationServiceListener {

    fun onDeviceRegistered(device: Device?)

    fun onDevice(device: Device?)

    fun onDeviceList(deviceList: List<Device>?)

    fun onGetDeviceListError()

    fun onDeviceCertificateUpdated(device: Device?)

    fun onMKeyResponseError()
}