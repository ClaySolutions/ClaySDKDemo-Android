package com.salto.claysdkdemo.main.presenters

import android.app.Activity
import android.content.Intent
import com.myclay.claysdk.api.error.ClayException
import com.salto.claysdkdemo.base.IBasePresenter
import com.salto.claysdkdemo.enums.MKActivationState

interface IMainPresenter {

    interface View: IBasePresenter.View {

        fun startLogoutIntent(intent: Intent)

        fun onActivationStateChanged(state: MKActivationState?)

        fun requestMissingPermissions(permissions: Array<String>, requestCode: Int)

        fun onNeverAskLocationPermissionAgain()

        fun onPeripheralFound()

        fun onSuccessWithCancelledKey()

        fun onKeySuccessfullySent()

        fun onMKeyDecryptionFailed()

        fun onBluetoothStatusChanged(enabled: Boolean)

        fun onKeySendError(errorMessage: String, exception: ClayException)

        fun onTimeOut()

        fun onMobileKeyNotFound()

        fun onMissingLocationPermission()

        fun onPermissionGranted()
    }

    interface Action: IBasePresenter.Action<View> {

        fun logout()

        fun checkOrRegisterDevice(): Boolean

        fun registerDevice()

        fun deleteDevice()

        fun openLock()

        fun onRequestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray, activity: Activity)

        fun onGoToSettingsClick()

        fun onResume()
    }
}