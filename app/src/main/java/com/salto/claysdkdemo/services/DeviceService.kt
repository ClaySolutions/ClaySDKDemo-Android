package com.salto.claysdkdemo.services

import com.salto.claysdkdemo.application.AppConfig
import com.salto.claysdkdemo.listeners.IDeviceServiceListener
import com.salto.claysdkdemo.models.APIListResponse
import com.salto.claysdkdemo.models.Device
import com.salto.claysdkdemo.retrofit.IDeviceRetrofit
import java.util.HashMap

class DeviceService(private val retrofit: IDeviceRetrofit, private val authenticator: IOIDAuthenticator) : IDeviceService {

    private var deviceServiceListener: IDeviceServiceListener? = null
        set(value) {
            field = value
            authenticator.setAuthenticationDelegate(deviceServiceListener)
        }

    val deviceCallback = object : ApiCallback<Device>() {

        override fun onSuccess(body: Device?) {
            deviceServiceListener?.onDevice(body)
        }

        override fun onError(message: String?) {
            deviceServiceListener?.onMKeyResponseError()
        }
    }

    override fun registerDevice(deviceName: String, deviceUid: String, publicKey: String) {
        authenticator.execute(object : AuthCallback() {

            override fun onSuccess() {
                val deviceInfoMap = HashMap<String, String>()
                deviceInfoMap[AppConfig.ApiParams.DEVICE_UUID] = deviceUid
                deviceInfoMap[AppConfig.ApiParams.DEVICE_NAME] = deviceName
                deviceInfoMap[AppConfig.ApiParams.PUBLIC_KEY] = publicKey
                retrofit.registerDevice(deviceInfoMap).enqueue(object : ApiCallback<Device>() {

                    override fun onSuccess(body: Device?) {
                        deviceServiceListener?.onDeviceRegistered(body)
                    }

                    override fun onError(message: String?) {
                        deviceServiceListener?.onMKeyResponseError()
                    }
                })
            }
        })
    }

    override fun getMobileKey(userId: String) {
        authenticator.execute(object : AuthCallback() {

            override fun onSuccess() {
                retrofit.getMobileKey(userId).enqueue(deviceCallback)
            }
        })
    }

    override fun getDeviceList(odataFilter: String) {
        authenticator.execute(object : AuthCallback() {

            override fun onSuccess() {
                retrofit.getDeviceList(odataFilter).enqueue(object : ApiCallback<APIListResponse<Device>>() {

                    override fun onSuccess(body: APIListResponse<Device>?) {
                        deviceServiceListener?.onDeviceList(body?.list)
                    }

                    override fun onError(message: String?) {
                        deviceServiceListener?.onGetDeviceListError()
                    }
                })
            }
        })
    }

    override fun getDevice(deviceId: String) {
        authenticator.execute(object : AuthCallback() {

            override fun onSuccess() {
                retrofit.getDevice(deviceId).enqueue(deviceCallback)
            }
        })
    }

    override fun putDeviceCertificate(userId: String, publicKey: String) {
        authenticator.execute(object : AuthCallback() {

            override fun onSuccess() {
                val deviceInfoMap = HashMap<String, String>()
                deviceInfoMap[AppConfig.ApiParams.PUBLIC_KEY] = publicKey
                retrofit.putDeviceCertificate(userId, deviceInfoMap).enqueue(object : ApiCallback<Device>() {

                    override fun onSuccess(body: Device?) {
                        deviceServiceListener?.onDeviceCertificateUpdated(body)
                    }

                    override fun onError(message: String?) {
                        deviceServiceListener?.onMKeyResponseError()
                    }
                })
            }
        })
    }

    override fun setMKeyActivationServiceListener(deviceServiceListener: IDeviceServiceListener) {
        this.deviceServiceListener = deviceServiceListener
    }
}