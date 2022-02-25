package com.salto.claysdkdemo.access_code.presenters

import android.content.Context
import com.myclay.claysdk.api.ClaySDK
import com.myclay.claysdk.api.IClaySDK
import com.salto.claysdkdemo.application.ISharedPrefsUtil
import com.salto.claysdkdemo.base.BasePresenter
import com.salto.claysdkdemo.listeners.IDeviceServiceListener
import com.salto.claysdkdemo.listeners.IPodServiceListener
import com.salto.claysdkdemo.login.oid.OIDConfig
import com.salto.claysdkdemo.models.Device
import com.salto.claysdkdemo.models.GuestDigitalKey
import com.salto.claysdkdemo.services.IDeviceService
import com.salto.claysdkdemo.services.IPodService
import com.salto.claysdkdemo.utils.DeviceInfo
import net.openid.appauth.*
import java.util.*

class AccessCodePresenter(
    context: Context, sharedPrefs: ISharedPrefsUtil,
    private val authorizationService: AuthorizationService,
    private val podService: IPodService,
    private val deviceService: IDeviceService,
    private val apiKey: String,
    private val oidConfig: OIDConfig, private val authState: AuthState
) : BasePresenter<IAccessCodePresenter.View>(context, sharedPrefs), IAccessCodePresenter.Action,
    IPodServiceListener, IDeviceServiceListener {

    private var guestDigitalKey = GuestDigitalKey()

    private var firstName: String = ""
    private var lastName: String = ""
    private var accessCode: String = ""

    override fun didTapSave(accessCode: String?, firstName: String?, lastName: String?) {
        if (accessCode == null || firstName == null || lastName == null) {
            return
        }

        authState.authorizationServiceConfiguration?.let {
            val tokenRequest = TokenRequest.Builder(it, oidConfig.dkgClientId)
                .setGrantType("access_code")
                .setAdditionalParameters(hashMapOf<String, String?>("access_code" to accessCode))
                .build()
            authorizationService.performTokenRequest(
                tokenRequest,
                object : AuthorizationService.TokenResponseCallback {
                    override fun onTokenRequestCompleted(
                        response: TokenResponse?,
                        ex: AuthorizationException?
                    ) {
                        this@AccessCodePresenter.firstName = firstName
                        this@AccessCodePresenter.lastName = lastName
                        handleGuestDigitalKeyAccessTokenResponse(accessCode, response, ex)
                    }

                })
        }
    }

    private fun registerPodGuest(firstName: String, lastName: String) {
        podService.setServiceListener(this)
        podService.registerPodGuest(firstName, lastName)
    }

    private fun handleGuestDigitalKeyAccessTokenResponse(
        accessCode: String,
        tokenResponse: TokenResponse?,
        authException: AuthorizationException?
    ) {
        authException?.let {
            view?.didGetError(it.error)
            return
        }
        tokenResponse?.accessToken?.let {
            this.accessCode = accessCode
            authState.update(tokenResponse, authException)
            sharedPrefs.writeAuthState(authState)
            registerPodGuest(firstName, lastName)
            return
        }
        view?.didGetError()
    }

    override fun onPodGuestRegistered() {
        guestDigitalKey.let {
            it.firstName = firstName
            it.lastName = lastName
        }
        deviceService.setMKeyActivationServiceListener(this)

        val pseudoID = UUID.randomUUID().toString()
        val claySDK = ClaySDK.init(context, apiKey, pseudoID)

        deviceService.registerDevice(
            DeviceInfo.model,
            pseudoID,
            claySDK.publicKey
        )
    }

    override fun didReceivePodGuestRegisterError(error: String?) {
        view?.didGetError(error)
    }

    override fun didReceiveAuthState(state: AuthState?) = Unit

    override fun didReceiveAuthenticationError() = Unit

    override fun didReceiveAuthenticationFailure() = Unit

    override fun onDeviceRegistered(device: Device?) {
        device?.let {
            guestDigitalKey.device = it
            deviceService.getMobileKey(device.id)
        }
    }

    override fun onDevice(device: Device?) {
        guestDigitalKey.apply {
            mKeyData = device?.mKeyData
            dateCreated = Date()
            sharedPrefs.saveGuestDigitalKey(guestDigitalKey)
        }
        sharedPrefs.deleteAuthState()
        view?.didSaveGDKey()
    }

    override fun onDeviceList(deviceList: List<Device>?) = Unit

    override fun onGetDeviceListError() = Unit

    override fun onDeviceCertificateUpdated(device: Device?) = Unit

    override fun onMKeyResponseError() {
        view?.didGetError()
    }
}