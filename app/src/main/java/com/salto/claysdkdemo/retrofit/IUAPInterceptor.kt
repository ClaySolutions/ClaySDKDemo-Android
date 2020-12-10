package com.salto.claysdkdemo.retrofit

import okhttp3.Interceptor

interface IUAPInterceptor: Interceptor {

    val userAgentHeader: String

    val accessToken: String?
}