package com.salto.claysdkdemo.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
data class GuestDigitalKey(var device: Device?, var firstName: String?, var lastName: String?, var dateCreated: Date?, var mKeyData: String?): Parcelable {
    constructor() : this(null, null, null, null, null)

    fun getDateString(): String {
        //simple date formatter
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        //return the formatted date string
        return dateFormatter.format(dateCreated)
    }
}