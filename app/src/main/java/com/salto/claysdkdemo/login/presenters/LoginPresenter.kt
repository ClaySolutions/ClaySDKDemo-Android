package com.salto.claysdkdemo.login.presenters

import android.content.Context
import com.salto.claysdkdemo.application.ISharedPrefsUtil
import com.salto.claysdkdemo.base.BasePresenter
import com.salto.claysdkdemo.login.oid.AuthOIDError
import com.salto.claysdkdemo.login.oid.IOIDConfig
import net.openid.appauth.*

class LoginPresenter(context: Context, sharedPrefs: ISharedPrefsUtil,
                     private val authorizationService: AuthorizationService, private val authState: AuthState,
                     private val oidConfig: IOIDConfig)
    : BasePresenter<ILoginPresenter.View>(context, sharedPrefs), ILoginPresenter.Action {

    override val isLoggedIn: Boolean
        get() = sharedPrefs.readAuthState() != null

    override fun login() {
        val authorizationRequest = oidConfig.getAuthorizationRequest(authState) ?: run {
            view?.onOIDConfigError()
            return
        }
        if (authorizationService.browserDescriptor == null) {
            view?.displayBrowserError()
            return
        }
        val authIntent = authorizationService.getAuthorizationRequestIntent(authorizationRequest)
                ?: return
        view?.displayOpenIDIntent(authIntent)
    }

    override fun exchangeToken(authResponse: AuthorizationResponse?, authException: AuthorizationException?) {
        authException?.let {
            view?.displayError(AuthOIDError(it).toString())
            return
        }
        if (authResponse?.authorizationCode == null) {
            view?.displayError(AuthOIDError(AuthorizationException.AuthorizationRequestErrors.OTHER).toString())
            return
        }
        authState.update(authResponse, authException)
        sharedPrefs.writeAuthState(authState)
        authorizationService.performTokenRequest(authResponse.createTokenExchangeRequest()) {
            tokenResponse: TokenResponse?, exception: AuthorizationException? -> handleAccessTokenResponse(tokenResponse, exception)
        }
    }

    private fun handleAccessTokenResponse(tokenResponse: TokenResponse?, authException: AuthorizationException?) {
        authException?.let {
            view?.displayError(AuthOIDError(it).toString())
        }
        tokenResponse?.accessToken?.let {
            authState.update(tokenResponse, authException)
            sharedPrefs.writeAuthState(authState)
            view?.onLoginSuccess()
            return
        }
        view?.onLoginError()
    }
}