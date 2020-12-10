package com.salto.claysdkdemo.services

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class ApiCallback<T>: Callback<T> {

    override fun onResponse(call: Call<T>, response: Response<T>) {
        if(response.isSuccessful) {
            onSuccess(response.body())
            return
        }
        onError(response.message())
    }

    abstract fun onSuccess(body: T?)

    abstract fun onError(message: String?)

    override fun onFailure(call: Call<T>, t: Throwable) {
        onError(t.localizedMessage)
    }
}