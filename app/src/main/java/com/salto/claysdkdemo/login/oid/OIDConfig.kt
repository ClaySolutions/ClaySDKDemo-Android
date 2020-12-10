package com.salto.claysdkdemo.login.oid

import android.content.Context
import android.net.Uri
import android.util.Log
import com.quality.claysdkdemo.R
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.ResponseTypeValues
import javax.inject.Inject

class OIDConfig @Inject constructor(context: Context, private val idsUrl: String) : IOIDConfig {

    object Endpoint {
        const val AUTHORIZATION = "/connect/authorize"
        const val TOKEN = "/connect/token"
        const val END_SESSION = "/connect/endsession"
    }

    object OID {
        const val PARAM_ID_TOKEN_HINT = "id_token_hint"
        const val PARAM_REDIRECT_URI = "post_logout_redirect_uri"
    }

    private val clientId: String = context.getString(R.string.client_id)

    private val redirect: String = context.getString(R.string.redirect_url)
    private val logoutRedirect: String = context.getString(R.string.logout_redirect_url)
    private val scope = "openid profile offline_access user_api.full_access"

    override fun getLogoutRequest(authState: AuthState): String {
        val endSessionEndpoint =
            Uri.parse("${idsUrl}${Endpoint.END_SESSION}")

        val request = endSessionEndpoint.buildUpon()
            .appendQueryParameter(
                OID.PARAM_ID_TOKEN_HINT,
                authState.idToken
            )
            .appendQueryParameter(
                OID.PARAM_REDIRECT_URI,
                logoutRedirect
            )
            .build()
        return request.toString()
    }

    override fun getAuthorizationRequest(authState: AuthState): AuthorizationRequest? {
        val config = authState.authorizationServiceConfiguration ?: return null
        return try {
            val authRequestBuilder = AuthorizationRequest.Builder(
                config,
                clientId,
                ResponseTypeValues.CODE,
                Uri.parse(redirect)
            )
            authRequestBuilder
                .setScope(scope)
                .build()
        } catch (e: IllegalArgumentException) {
            Log.e("Error", e.localizedMessage ?: e.toString())
            null
        }
    }
}