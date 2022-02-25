package com.salto.claysdkdemo.retrofit

import com.salto.claysdkdemo.application.AppConfig
import com.salto.claysdkdemo.application.AppConfig.ApiParams.DEVICE_ID
import com.salto.claysdkdemo.application.AppConfig.ApiParams.DEVICE_UUID
import com.salto.claysdkdemo.application.AppConfig.ApiParams.USER_ID
import com.salto.claysdkdemo.models.APIListResponse
import com.salto.claysdkdemo.models.Device
import retrofit2.Call
import retrofit2.http.*

interface IDeviceRetrofit {

    @POST(AppConfig.Endpoints.REGISTER_MKEY_DEVICE)
    fun registerDevice(@Body deviceInfo: HashMap<String, String>): Call<Device>

    @GET(AppConfig.Endpoints.GET_MOBILE_KEY)
    fun getMobileKey(@Path(DEVICE_ID) deviceId: String): Call<Device>

    @GET(AppConfig.Endpoints.MKEY_DEVICE)
    fun getDevice(@Path(DEVICE_ID) deviceId: String): Call<Device>

    @GET(AppConfig.Endpoints.MKEY_DEVICE_LIST)
    fun getDeviceList(@Query(AppConfig.ApiParams.ODATA_FILTER) deviceUuidOData: String): Call<APIListResponse<Device>>

    @PUT(AppConfig.Endpoints.PUT_MKEY_DEVICE_CERTIFICATE)
    fun putDeviceCertificate(@Path(USER_ID) userId: String, @Body params: HashMap<String, String>): Call<Device>
}