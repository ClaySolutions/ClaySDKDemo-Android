package com.salto.claysdkdemo.access_code.presenters

import com.salto.claysdkdemo.base.IBasePresenter
import com.salto.claysdkdemo.models.GuestDigitalKey
import net.openid.appauth.AuthorizationException
import net.openid.appauth.TokenResponse

interface IAccessCodePresenter {

    interface View: IBasePresenter.View {
        fun didSaveGDKey()
        fun didGetError(error: String? = null)
    }

    interface Action: IBasePresenter.Action<View> {
        fun didTapSave(accessCode: String?, firstName: String?, lastName: String?)
    }
}