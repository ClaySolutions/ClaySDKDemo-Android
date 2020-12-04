package com.salto.claysdkdemo.services

import android.os.Handler
import com.salto.claysdkdemo.listeners.IAuthenticationServiceListener
import com.salto.claysdkdemo.models.OAuthAccessToken
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationService.TokenResponseCallback
import java.util.concurrent.LinkedBlockingQueue


class OIDAuthenticator(
    authorizationService: AuthorizationService, state: AuthState, private val handler: Handler
) :
    IOIDAuthenticator, IAuthenticationServiceListener {

    private var iAuthenticationServiceListener: IAuthenticationServiceListener? = null

    private var isRunning = false

    private val mQueue: LinkedBlockingQueue<AuthCallback> = LinkedBlockingQueue<AuthCallback>()

    private val mAuthState: AuthState = state

    private val mAuthorizationService: AuthorizationService = authorizationService

    override fun setAuthenticationDelegate(listener: IAuthenticationServiceListener?) {
        iAuthenticationServiceListener = listener
    }

    override fun execute(callback: AuthCallback?) {
        mQueue.add(callback)
        doNext()
    }

    private fun doNext() {
        if (isRunning) {
            return
        }
        val operation: AuthCallback? = mQueue.poll()
        isRunning = operation != null
        if (!isRunning) {
            return
        }
        if (mAuthState.needsTokenRefresh) {
            performTokenRefresh(operation)
            return
        }
        onSuccess(operation)
    }

    private fun onSuccess(callback: AuthCallback?) {
        isRunning = false
        handler.post {
            callback?.onSuccess()
            doNext()
        }
    }

    private fun onAuthenticationError() {
        isRunning = false
        handler.post {
            didReceiveAuthenticationError()
            emptyQueue()
        }
    }

    private fun emptyQueue() {
        while (!mQueue.isEmpty()) {
            mQueue.poll()
        }
    }

    private fun performTokenRefresh(operation: AuthCallback?) {
        if (mAuthState.refreshToken == null) {
            onAuthenticationError()
            return
        }
        Thread {
            try {
                mAuthorizationService.performTokenRequest(
                    mAuthState.createTokenRefreshRequest(),
                    TokenResponseCallback { tokenResponse, ex ->
                        if (ex != null) {
                            onAuthenticationError()
                            return@TokenResponseCallback
                        }
                        mAuthState.update(tokenResponse, ex)
                        didReceiveAuthState(mAuthState)
                        onSuccess(operation)
                    })
            } catch (e: Exception) {
                onAuthenticationError()
            }
        }.start()
    }

    override fun didReceiveAuthenticationToken(token: OAuthAccessToken?) {
        iAuthenticationServiceListener?.didReceiveAuthenticationToken(token)
    }

    override fun didReceiveAuthState(state: AuthState?) {
        iAuthenticationServiceListener?.didReceiveAuthState(state)
    }

    override fun didReceiveAuthenticationError() {
        iAuthenticationServiceListener?.didReceiveAuthenticationError()
    }

    override fun didReceiveAuthenticationFailure() {
        iAuthenticationServiceListener?.didReceiveAuthenticationFailure()
    }

}