package com.salto.claysdkdemo.main.presenters

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import androidx.annotation.RequiresApi
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.myclay.claysdk.api.IClaySDK
import com.myclay.claysdk.api.ILockDiscoveryCallback
import com.myclay.claysdk.api.error.ClayErrorCode
import com.myclay.claysdk.api.error.ClayException
import com.quality.claysdkdemo.R
import com.salto.claysdkdemo.application.AppConfig
import com.salto.claysdkdemo.application.ISharedPrefsUtil
import com.salto.claysdkdemo.base.BasePresenter
import com.salto.claysdkdemo.enums.MKActivationState
import com.salto.claysdkdemo.listeners.IDeviceServiceListener
import com.salto.claysdkdemo.login.oid.IOIDConfig
import com.salto.claysdkdemo.models.Device
import com.salto.claysdkdemo.services.IDeviceService
import com.salto.claysdkdemo.utils.DeviceInfo
import com.saltosystems.justinmobile.sdk.common.OpResult
import com.saltosystems.justinmobile.sdk.exceptions.JustinErrorCodes
import com.saltosystems.justinmobile.sdk.model.Result
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthState
import net.openid.appauth.browser.BrowserSelector

/**
 * Background permission is required since Android OS Q if the opening operation
 * needs to be performed on background. Case scenario of a widget for example.
 * Toggle to false if that is not the case
 */
const val NEEDS_BACKGROUND_PERMISSION: Boolean = true

class MainPresenter(context: Context, sharedPrefs: ISharedPrefsUtil, private val authState: AuthState, private val oidConfig: IOIDConfig,
                    private val deviceService: IDeviceService, private val claySDK: IClaySDK, private val handler: Handler
): BasePresenter<IMainPresenter.View>(context, sharedPrefs), IMainPresenter.Action,
    IDeviceServiceListener {

    private var isBluetoothTurnedOn: Boolean = false
    private var lastOpeningStartTimestamp: Long = 0
    private var isOpening: Boolean = false
    private var isInErrorState: Boolean = false
    private var isActivityPaused: Boolean = false
    private var isLocationPermissionDenied = false

    init {
        deviceService.setMKeyActivationServiceListener(this)
    }

    private var activationState: MKActivationState? = null
        set(value) {
            field = value
            view?.onActivationStateChanged(value)
        }

    private val lockDiscoveryCallback = object : ILockDiscoveryCallback {

        override fun onPeripheralFound() {
            view?.onPeripheralFound()
            isOpening = false
        }

        override fun onSuccess(result: Result) {
            isOpening = false
            if (result.opResult == OpResult.AUTH_SUCCESS_CANCELLED_KEY) {
                view?.onSuccessWithCancelledKey()
                isInErrorState = true
                return
            }
            view?.onKeySuccessfullySent()
        }

        override fun onFailure(exception: ClayException) {
            handleClayException(exception)
        }
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            val action: String? = intent?.action
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                    BluetoothAdapter.STATE_TURNING_ON -> {
                        isBluetoothTurnedOn = true
                        scanOnBluetoothTurnedOn(AppConfig.Timers.MKEY_BLUETOOTH_ON_RETRY_DOUBLE)
                    }
                }
            }
        }
    }

    private fun scanOnBluetoothTurnedOn(delay: Long) {
        handler.postDelayed({
                    if (!isActivityPaused) {
                        view?.onBluetoothStatusChanged(true)
                    } }, delay)
    }

    private fun handleClayException(exception: ClayException) {
        try {
            if (exception.errorCode == ClayErrorCode.DECRYPT_FAILED_ERROR.value) {
                view?.onMKeyDecryptionFailed()
                isInErrorState = true
                return
            }
            if (exception.errorCode == ClayException.ErrorCodes.BLUETOOTH_NOT_INITIALIZED_ERROR) {
                view?.onBluetoothStatusChanged(false)
                isInErrorState = true
                return
            }
            if (exception.errorCode == JustinErrorCodes.PROCESS_ALREADY_RUNNING_ERROR) {
                forceNewOpening()
                return
            }
            if (exception.errorCode != JustinErrorCodes.TIMEOUT_REACHED_ERROR) {
                view?.onKeySendError(getErrorMessage(exception), exception)
                isInErrorState = true
                return
            }
            val time = System.currentTimeMillis() - lastOpeningStartTimestamp
            if ((time > AppConfig.Timers.MKEY_TIMEOUT_THRESHOLD)) {
                view?.onTimeOut()
                return
            }
            forceNewOpening()
        } finally {
            isOpening = false
        }
    }

    private fun getErrorMessage(exception: ClayException): String {
        if (exception.errorCode == ClayException.ErrorCodes.COARSE_LOCATION_PERMISSION_DENIED_ERROR) {
            return context.getString(R.string.mkey_coarse_location_permission)
        }
        if (isLocationNotEnabledError(exception)) {
            return context.getString(R.string.mkey_coarse_location_not_enabled)
        }
        return exception.localizedMessage ?: ""
    }

    private fun isLocationNotEnabledError(exception: ClayException): Boolean {
        return exception.errorCode == ClayException.ErrorCodes.COARSE_LOCATION_NOT_ENABLED_ERROR ||
                exception.errorCode == JustinErrorCodes.FINE_LOCATION_NOT_ENABLED_ERROR
    }

    private fun forceNewOpening() {
        isOpening = false
        handler.postDelayed({ openLock() }, 200)
    }

    override fun logout() {
        val request: String = oidConfig.getLogoutRequest(authState)
        val intent: Intent = buildLogoutIntent(request)
        sharedPrefs.deleteAuthState()
        view?.startLogoutIntent(intent)
    }

    override fun checkOrRegisterDevice(): Boolean {
        sharedPrefs.device ?: run {
            getDeviceList()
            return false
        }
        return true
    }

    private fun getDeviceList() {
        activationState = MKActivationState.REGISTERING
        deviceService.getDeviceList(AppConfig.ApiParams.ODATA_FILTER_DEVICE_UUID
                .replace(AppConfig.ApiParams.DEVICE_UUID_PLACE_HOLDER, DeviceInfo.uniquePseudoID(context)))
    }

    override fun registerDevice() {
        deleteDevice()
        getDeviceList()
    }

    private fun buildLogoutIntent(request: String): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setPackage(BrowserSelector.select(context, AppAuthConfiguration.DEFAULT.browserMatcher)?.packageName)
        intent.data = Uri.parse(request)
        intent.putExtra(CustomTabsIntent.EXTRA_TITLE_VISIBILITY_STATE, CustomTabsIntent.NO_TITLE)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        return intent
    }

    override fun onDeviceRegistered(device: Device?) {
        activationState = MKActivationState.DOWNLOADING
        device?.id?.let {
            downloadMKey(it)
        } ?: run { activationState = MKActivationState.ERROR }
    }

    override fun onDevice(device: Device?) {
        activationState = MKActivationState.ACTIVATED
        sharedPrefs.device = device
    }

    override fun onDeviceList(deviceList: List<Device>?) {
        if (deviceList.isNullOrEmpty()) {
            register()
            return
        }
        deviceList.firstOrNull { device -> device.deviceUid == DeviceInfo.uniquePseudoID(context) }
            ?.let { device ->
                putDeviceCertificate(device.id)
            } ?: register()
    }

    private fun putDeviceCertificate(deviceId: String) {
        try {
            deviceService.putDeviceCertificate(deviceId, claySDK.publicKey)
        } catch (e: ClayException) {
            onMKeyResponseError()
        }
    }

    private fun register() {
        activationState = MKActivationState.REGISTERING
        try {
            deviceService.registerDevice(DeviceInfo.model, DeviceInfo.uniquePseudoID(context), claySDK.publicKey)
        } catch (e: ClayException) {
            onMKeyResponseError()
        }
    }

    override fun onGetDeviceListError() {
        register()
    }

    override fun deleteDevice() {
        sharedPrefs.device = null
    }

    override fun openLock() {
        if(!checkAndAskRequiredPermission()) {
            return
        }
        if (isActivityPaused || isOpening) {
            return
        }
        isOpening = true

        sharedPrefs.device?.mKeyData?.let { mKey ->
            try {
                lastOpeningStartTimestamp = System.currentTimeMillis()
                claySDK.openDoor(mKey, lockDiscoveryCallback)
            } catch (exception: ClayException) {
                handleClayException(exception)
            }
            return
        }
        isOpening = false
        view?.onMobileKeyNotFound()
    }

    override fun onResume() {
        if (isLocationPermissionDenied) {
            return
        }

        if (isInErrorState) {
            isActivityPaused = false
            isInErrorState = false
            openLock()
        }

        if (isActivityPaused && isBluetoothTurnedOn) {
            scanOnBluetoothTurnedOn(AppConfig.Timers.MKEY_BLUETOOTH_ON_RETRY)
            isActivityPaused = false
            isBluetoothTurnedOn = false
        }

        context.registerReceiver(
                bluetoothReceiver,
                IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        activity: Activity
    ) {
        when {
            grantResults.isEmpty() -> onMissingLocationPermission()

            grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                isActivityPaused = false
                isLocationPermissionDenied = false
                view?.onPermissionGranted()
            }

            Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> onMissingLocationPermission()

            permissions.isNotEmpty() && !activity.shouldShowRequestPermissionRationale(permissions[0]) -> {
                isLocationPermissionDenied = true
                isInErrorState = true
                view?.onNeverAskLocationPermissionAgain()
            }

            else -> onMissingLocationPermission()
        }
    }

    override fun onGoToSettingsClick() {
        isLocationPermissionDenied = false
    }

    private fun onMissingLocationPermission() {
        isLocationPermissionDenied = true
        view?.onMissingLocationPermission()
    }

    private fun hasLocationPermissions(): Boolean {
        val locationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        return locationPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun checkAndAskRequiredPermission(): Boolean {
        if (isLocationPermissionDenied) {
            view?.onNeverAskLocationPermissionAgain()
            return false
        }

        if (hasLocationPermissions()) {
            return true
        }

        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        view?.requestMissingPermissions(permissions, AppConfig.RequestCodes.REQUEST_FINE_LOCATION_PERMISSION)
        return false
    }

    override fun onDeviceCertificateUpdated(device: Device?) {
        device?.id?.let {
            downloadMKey(it)
        } ?: run { activationState = MKActivationState.ERROR }
    }

    private fun downloadMKey(deviceId: String) {
        activationState = MKActivationState.DOWNLOADING
        deviceService.getMobileKey(deviceId)
    }

    override fun onMKeyResponseError() {
        activationState = MKActivationState.ERROR
    }

    override fun didReceiveAuthState(state: AuthState?) {
        sharedPrefs.writeAuthState(state)
    }

    override fun didReceiveAuthenticationError() {
        logout()
    }

    override fun didReceiveAuthenticationFailure() {
        logout()
    }
}