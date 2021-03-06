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
import com.myclay.claysdk.api.ClaySDK
import com.myclay.claysdk.api.IClaySDK
import com.myclay.claysdk.api.ILockDiscoveryCallback
import com.myclay.claysdk.api.error.ClayErrorCode
import com.myclay.claysdk.api.error.ClayException
import com.salto.claysdkdemo.R
import com.salto.claysdkdemo.application.AppConfig
import com.salto.claysdkdemo.application.ISharedPrefsUtil
import com.salto.claysdkdemo.base.BasePresenter
import com.salto.claysdkdemo.enums.MKActivationState
import com.salto.claysdkdemo.listeners.IDeviceServiceListener
import com.salto.claysdkdemo.login.oid.IOIDConfig
import com.salto.claysdkdemo.models.Device
import com.salto.claysdkdemo.models.GuestDigitalKey
import com.salto.claysdkdemo.services.IDeviceService
import com.salto.claysdkdemo.utils.DeviceInfo
import com.saltosystems.justinmobile.sdk.common.OpResult
import com.saltosystems.justinmobile.sdk.exceptions.JustinErrorCodes
import com.saltosystems.justinmobile.sdk.model.Result
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthState
import net.openid.appauth.browser.BrowserSelector


class MainPresenter(context: Context, sharedPrefs: ISharedPrefsUtil, private val authState: AuthState, private val oidConfig: IOIDConfig,
                    private val deviceService: IDeviceService, private val apiKey: String, private val claySDK: IClaySDK, private val handler: Handler
): BasePresenter<IMainPresenter.View>(context, sharedPrefs), IMainPresenter.Action,
    IDeviceServiceListener {

    private var isBluetoothTurnedOn: Boolean = false
    private var isOpening: Boolean = false
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

        /**
         * Called when a lock is detected
         */
        override fun onPeripheralFound() {
            view?.onPeripheralFound()
            isOpening = false
        }

        /**
         * Called when the Mobile Key is correctly sent to the lock
         * Optionally result could give info about the opening operation
         */
        override fun onSuccess(result: Result) {
            isOpening = false
            if (result.opResult == OpResult.AUTH_SUCCESS_CANCELLED_KEY) {
                view?.onSuccessWithCancelledKey()
                return
            }
            view?.onKeySuccessfullySent()

            when (OpResult.getGroup(result.opResult)) {

                OpResult.Group.ACCEPTED -> view?.onKeyAccepted()

                OpResult.Group.UNKNOWN_RESULT -> Unit

                else -> view?.onKeyRejectedOrFailing()
            }

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
            view?.onBluetoothStatusChanged(true)
        }, delay)
    }

    private fun handleClayException(exception: ClayException) {
        isOpening = false
        when (exception.errorCode) {

            ClayErrorCode.DECRYPT_FAILED_ERROR.value -> view?.onMKeyDecryptionFailed()

            ClayException.ErrorCodes.BLUETOOTH_NOT_INITIALIZED_ERROR -> view?.onBluetoothStatusChanged(false)

            JustinErrorCodes.PROCESS_ALREADY_RUNNING_ERROR -> forceNewOpening()

            JustinErrorCodes.TIMEOUT_REACHED_ERROR -> view?.onTimeOut()

            else -> view?.onKeySendError(getErrorMessage(exception), exception)

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

    // When the normal flow is used (login -> regular digital key), guestDigitalKey should be left empty/null, parameter key is only used for digital key for guests.
    override fun openLock(needsBackground: Boolean, key: GuestDigitalKey?) {
        if(!checkAndAskRequiredPermission(needsBackground)) {
            return
        }
        if (isOpening) {
            return
        }
        isOpening = true
        key?.let {
            // Guest Digital Key Flow;
            try {
                val guestKeyClaySDK = ClaySDK.init(context, apiKey, key.device?.deviceUid)
                guestKeyClaySDK.openDoor(it.mKeyData, lockDiscoveryCallback) //Opening start
            } catch (exception: ClayException) {
                handleClayException(exception)
            }
            return
        } ?: run {
            // Default flow (via login);
            sharedPrefs.device?.mKeyData?.let { mKey ->
                try {
                    claySDK.openDoor(mKey, lockDiscoveryCallback) //Opening start
                } catch (exception: ClayException) {
                    handleClayException(exception)
                }
                return
            }
        }

        isOpening = false
        view?.onMobileKeyNotFound()
    }

    override fun onResume() {
        if (isBluetoothTurnedOn) {
            scanOnBluetoothTurnedOn(AppConfig.Timers.MKEY_BLUETOOTH_ON_RETRY)
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
                isLocationPermissionDenied = false
                view?.onPermissionGranted()
            }

            Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> onMissingLocationPermission()

            permissions.isNotEmpty() && !activity.shouldShowRequestPermissionRationale(permissions[0]) -> {
                isLocationPermissionDenied = true
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

    override fun hasLocationPermissions(needsBackground: Boolean): Boolean {
        val locationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && needsBackground) {
            val backgroundLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            return locationPermission == PackageManager.PERMISSION_GRANTED && backgroundLocationPermission == PackageManager.PERMISSION_GRANTED
        }

        return locationPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun hasNearbyDeviceDetectionPermission(): Boolean {
        // When targeting Android 12 (SDK 31) and higher, new permissions are needed for bluetooth, therefore it's necessary to check for these specific permissions if user has this version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val hasPermissionBTScan = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
            val hasPermissionBTConnect = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            return hasPermissionBTScan && hasPermissionBTConnect
        }
        return true
    }

    override fun checkAndAskRequiredPermission(needsBackground: Boolean): Boolean {
        if (isLocationPermissionDenied) {
            view?.onNeverAskLocationPermissionAgain()
            return false
        }

        if (hasLocationPermissions(needsBackground) && hasNearbyDeviceDetectionPermission()) {
            return true
        }

        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
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