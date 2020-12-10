package com.salto.claysdkdemo.login.presenters

import android.content.Intent
import com.salto.claysdkdemo.base.IBasePresenter
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse

interface ILoginPresenter {

    interface View: IBasePresenter.View {

        fun onLoginSuccess()

        fun onLoginError()

        fun displayOpenIDIntent(authIntent: Intent)

        fun displayBrowserError()

        fun displayError(error: String)

        fun onOIDConfigError()
    }

    interface Action: IBasePresenter.Action<View> {

        fun login()

        fun exchangeToken(authResponse: AuthorizationResponse?, authException: AuthorizationException?)

        val isLoggedIn: Boolean
    }
}