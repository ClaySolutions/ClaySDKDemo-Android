package com.salto.claysdkdemo.models

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Device(

    @field:Expose @field:SerializedName("mkey_data")
    var mKeyData: String?,

    @field:Expose @field:SerializedName("certificate")
    val certificate: String,

    @field:Expose @field:SerializedName("id")
    val id: String,

    @field:Expose @field:SerializedName("device_name")
    val deviceName: String,

    @field:Expose @field:SerializedName("device_uid")
    val deviceUid: String,

    @field:Expose @field:SerializedName("mkey")
    val mKey: Key
) : Parcelable