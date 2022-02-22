package com.salto.claysdkdemo.retrofit

import com.salto.claysdkdemo.application.AppConfig
import com.salto.claysdkdemo.application.AppConfig.ApiParams.DEVICE_ID
import com.salto.claysdkdemo.application.AppConfig.ApiParams.DEVICE_UUID
import com.salto.claysdkdemo.application.AppConfig.ApiParams.USER_ID
import com.salto.claysdkdemo.models.APIListResponse
import com.salto.claysdkdemo.models.Device
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface IPodRetrofit {

    @POST(AppConfig.Endpoints.REGISTER_POD_GUEST)
    fun registerPodGuest(@Body params: HashMap<String, String>): Call<ResponseBody>
}