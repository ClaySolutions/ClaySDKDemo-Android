package com.salto.claysdkdemo.models

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Key(

    @field:Expose
    @field:SerializedName(value = "mkey_data", alternate = ["mkey"])
    var mKeyData: String? = null,

    var id: String? = null,

    @field:Expose
    @field:SerializedName("key_id")
    var keyId: String? = null,

    @field:Expose
    @field:SerializedName("expiry_date")
    var expiryDate: String? = null,

    @field:Expose
    @field:SerializedName("registration_date")
    var registrationDate: String? = null
) : Parcelable
