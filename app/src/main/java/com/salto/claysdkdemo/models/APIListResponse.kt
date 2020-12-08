package com.salto.claysdkdemo.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class APIListResponse<T>(

    @field:Expose
    @field:SerializedName(value = "items")
    var list: List<T>,

    @field:Expose
    @field:SerializedName(value = "next_page_link")
    var nextPage: String
) {

    @SerializedName(value = "count")
    @Expose
    var count = 0
}