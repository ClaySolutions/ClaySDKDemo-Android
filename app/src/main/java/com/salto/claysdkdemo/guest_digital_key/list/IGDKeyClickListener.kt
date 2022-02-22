package com.salto.claysdkdemo.guest_digital_key.list

import com.salto.claysdkdemo.models.GuestDigitalKey
import com.salto.claysdkdemo.models.Key

interface IGDKeyClickListener {
    fun clicked(key: GuestDigitalKey)
}