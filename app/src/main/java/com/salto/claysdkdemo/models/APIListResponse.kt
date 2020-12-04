package com.salto.claysdkdemo.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class APIListResponse<T>(

    @field:Expose
    @field:SerializedName(value = "value", alternate = ["items"])
    var list: List<T>,

    @field:Expose
    @field:SerializedName(value = "@odata.nextLink", alternate = ["next_page_link"])
    var nextPage: String
) {

    @SerializedName(value = "@odata.count", alternate = ["count"])
    @Expose
    var count = 0
}