package com.salto.claysdkdemo.guest_digital_key.presenters

import android.content.Context
import android.os.Handler
import com.salto.claysdkdemo.application.ISharedPrefsUtil
import com.salto.claysdkdemo.base.BasePresenter

class GuestDigitalKeysListPresenter(context: Context, sharedPrefs: ISharedPrefsUtil, private val handler: Handler
): BasePresenter<IGuestDigitalKeysListPresenter.View>(context, sharedPrefs),
    IGuestDigitalKeysListPresenter.Action {

    override fun fetchDigitalKeysForGuest() {
        val guestDigitalKeys = sharedPrefs.getGuestDigitalKeys()
        view?.onDigitalKeysForGuestRetrieved(guestDigitalKeys)
    }
}