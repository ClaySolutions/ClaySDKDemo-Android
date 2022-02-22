package com.salto.claysdkdemo.services

import com.salto.claysdkdemo.application.AppConfig
import com.salto.claysdkdemo.listeners.IPodServiceListener
import com.salto.claysdkdemo.retrofit.IPodRetrofit
import okhttp3.ResponseBody
import java.util.*

class PodService(private val retrofit: IPodRetrofit, private val authenticator: IOIDAuthenticator) :
    IPodService {

    private var podServiceListener: IPodServiceListener? = null
        set(value) {
            field = value
            authenticator.setAuthenticationDelegate(podServiceListener)
        }

    override fun setServiceListener(listener: IPodServiceListener) {
        this.podServiceListener = listener
    }

    override fun registerPodGuest(firstName: String, lastName: String) {
        authenticator.execute(object : AuthCallback() {

            override fun onSuccess() {
                val userMap = HashMap<String, String>()
                userMap[AppConfig.ApiParams.FIRST_NAME] = firstName
                userMap[AppConfig.ApiParams.LAST_NAME] = lastName
                retrofit.registerPodGuest(userMap).enqueue(object : ApiCallback<ResponseBody>() {

                    override fun onSuccess(body: ResponseBody?) {
                        podServiceListener?.onPodGuestRegistered()
                    }

                    override fun onError(message: String?) {
                        podServiceListener?.didReceivePodGuestRegisterError(message)
                    }
                })
            }
        })
    }
}