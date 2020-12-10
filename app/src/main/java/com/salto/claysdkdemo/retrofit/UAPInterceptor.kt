package com.salto.claysdkdemo.retrofit

import android.content.Context
import android.os.Build
import com.google.common.net.HttpHeaders.AUTHORIZATION
import com.google.common.net.HttpHeaders.USER_AGENT
import com.quality.claysdkdemo.BuildConfig
import com.quality.claysdkdemo.R
import com.salto.claysdkdemo.application.ISharedPrefsUtil
import okhttp3.Interceptor
import okhttp3.OkHttp
import okhttp3.Response
import java.text.SimpleDateFormat
import java.util.*

class UAPInterceptor(private val context: Context, private val sharedPrefs: ISharedPrefsUtil) :
    IUAPInterceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
            .header(AUTHORIZATION, "Bearer $accessToken")
            .header(USER_AGENT, userAgentHeader)
            .method(chain.request().method, chain.request().body)
        val request = requestBuilder.build()
        return chain.proceed(request)
    }

    override val userAgentHeader: String
        get() = String.format(
            "%s/%s (%s; %s; %s; Android %s) okhttp %s",
            getAppName(),
            BuildConfig.VERSION_NAME,
            BuildConfig.APPLICATION_ID,
            BuildConfig.VERSION_CODE,
            getVersionDate(),
            Build.VERSION.RELEASE,
            OkHttp.VERSION
        )

    private fun getVersionDate(): String {
        return SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(BuildConfig.BUILD_TIME.toLong())
    }

    override val accessToken: String?
        get() = sharedPrefs.readAuthState()?.accessToken

    private fun getAppName(): String {
        return context.getString(R.string.app_name)
    }
}