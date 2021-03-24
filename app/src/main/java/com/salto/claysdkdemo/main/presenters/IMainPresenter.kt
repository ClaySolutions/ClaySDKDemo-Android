package com.salto.claysdkdemo.main.presenters

import android.app.Activity
import android.content.Intent
import com.myclay.claysdk.api.error.ClayException
import com.salto.claysdkdemo.base.IBasePresenter
import com.salto.claysdkdemo.enums.MKActivationState

interface IMainPresenter {

    interface View: IBasePresenter.View {



        fun startLogoutIntent(intent: Intent) = Unit

        fun onActivationStateChanged(state: MKActivationState?) = Unit

        fun requestMissingPermissions(permissions: Array<String>, requestCode: Int) = Unit

        fun onNeverAskLocationPermissionAgain() = Unit

        fun onPeripheralFound() = Unit

        fun onSuccessWithCancelledKey() = Unit

        fun onKeySuccessfullySent() = Unit

        fun onMKeyDecryptionFailed() = Unit

        fun onBluetoothStatusChanged(enabled: Boolean) = Unit

        fun onKeySendError(errorMessage: String, exception: ClayException) = Unit

        fun onTimeOut() = Unit

        fun onMobileKeyNotFound() = Unit

        fun onMissingLocationPermission() = Unit

        fun onPermissionGranted() = Unit

        fun onKeyAccepted() = Unit

        fun onKeyRejectedOrFailing() = Unit
    }

    interface Action: IBasePresenter.Action<View> {

        fun logout()

        fun checkOrRegisterDevice(): Boolean

        fun registerDevice()

        fun deleteDevice()

        fun openLock(needsBackground: Boolean = false)

        fun onRequestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray, activity: Activity)

        fun onGoToSettingsClick()

        fun onResume()

        fun checkAndAskRequiredPermission(needsBackground: Boolean): Boolean

        fun hasLocationPermissions(needsBackground: Boolean): Boolean
    }
}