package com.salto.claysdkdemo.guest_digital_key.presenters

import com.salto.claysdkdemo.base.IBasePresenter
import com.salto.claysdkdemo.models.GuestDigitalKey

interface IGuestDigitalKeysListPresenter {

    interface View: IBasePresenter.View {

        fun onDigitalKeysForGuestRetrieved(keys: List<GuestDigitalKey>)
    }

    interface Action: IBasePresenter.Action<View> {
        fun fetchDigitalKeysForGuest()
    }
}