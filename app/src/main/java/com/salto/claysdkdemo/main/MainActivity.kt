package com.salto.claysdkdemo.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.app.ActivityCompat
import com.myclay.claysdk.api.error.ClayException
import com.quality.claysdkdemo.R
import com.salto.claysdkdemo.application.AppConfig
import com.salto.claysdkdemo.base.SaltoActivity
import com.salto.claysdkdemo.enums.MKActivationState
import com.salto.claysdkdemo.login.LoginActivity
import com.salto.claysdkdemo.main.presenters.IMainPresenter
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : SaltoActivity<IMainPresenter.View, IMainPresenter.Action>(),
    IMainPresenter.View {

    @Inject
    lateinit var handler: Handler

    private var status: String = ""
        set(value) {
            field = value
            status_tv.visibility = if(value.isEmpty()) GONE else VISIBLE
            status_tv.text = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        logout_button.setOnClickListener {
            presenter.logout()
        }
        retry_button.setOnClickListener {
            registerDevice()
        }
        open_button.setOnClickListener {
            openLock()
        }
        if(presenter.checkOrRegisterDevice()) {
            open_button.visibility = VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    private fun openLock() {
        showProgress()
        status = getString(R.string.looking_for_looks)
        open_button.visibility = GONE
        presenter.openLock()
    }

    private fun registerDevice() {
        presenter.registerDevice()
    }

    override fun bindView() {
        presenter.bindView(this)
    }

    override fun startLogoutIntent(intent: Intent) {
        startActivityForResult(intent, AppConfig.RequestCodes.LOGOUT)
    }

    override fun requestMissingPermissions(permissions: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(this, permissions, requestCode)
    }

    override fun onPermissionGranted() {
        openLock()
    }

    override fun onNeverAskLocationPermissionAgain() {
        status = getString(R.string.settings_location_permissions)
        setting_button.visibility = VISIBLE
        open_button.visibility = GONE
        progress_bar.visibility = GONE
        setting_button.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
            setting_button.visibility = GONE
            reset()
            presenter.onGoToSettingsClick()
        }
    }

    override fun onPeripheralFound() {
        status = getString(R.string.lock_found)
    }

    override fun onSuccessWithCancelledKey() {
        reactivateDevice()
    }

    private fun reactivateDevice() {
        status = getString(R.string.reactivate_mobile_key)
        open_button.visibility = GONE
        handler.postDelayed({ registerDevice() }, 2000)
    }

    override fun onKeySuccessfullySent() {
        status = getString(R.string.mobile_key_received_by_lock)
        reset()
    }

    private fun reset(delay: Long = 3000) {
        hideProgress()
        open_button.visibility = VISIBLE
        handler.postDelayed({
            status = ""
        }, delay)
    }

    override fun onMKeyDecryptionFailed() {
        reactivateDevice()
    }

    override fun onBluetoothStatusChanged(enabled: Boolean) {
        if (enabled) {
            openLock()
            return
        }
        status = getString(R.string.required_to_mobile_key)
    }

    override fun onKeySendError(errorMessage: String, exception: ClayException) {
        status = errorMessage
        reset()
    }

    override fun onTimeOut() {
        status = getString(R.string.lock_not_found)
        reset()
    }

    override fun onMobileKeyNotFound() {
        reactivateDevice()
    }

    override fun onMissingLocationPermission() {
        status = getString(R.string.mkey_coarse_location_permission)
        reset()
    }

    override fun onActivationStateChanged(state: MKActivationState?) {
        when(state) {
            MKActivationState.REGISTERING -> onDeviceRegistration()
            MKActivationState.DOWNLOADING -> onMobileKeyDownload()
            MKActivationState.ACTIVATED -> onDeviceActivated()
            MKActivationState.ERROR -> onRegistrationError()
        }
    }

    private fun onRegistrationError() {
        status = getString(R.string.activation_state_error)
        hideProgress()
        retry_button.visibility = VISIBLE
    }

    private fun showProgress() {
        progress_bar.visibility = VISIBLE
    }

    private fun hideProgress() {
        progress_bar.visibility = GONE
    }

    private fun onDeviceActivated() {
        status = getString(R.string.activation_state_active)
        reset(5000)
    }

    private fun onMobileKeyDownload() {
        showProgress()
        status = getString(R.string.activation_state_downloading)
    }

    private fun onDeviceRegistration() {
        status = getString(R.string.activation_state_registering)
        showProgress()
        retry_button.visibility = GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppConfig.RequestCodes.LOGOUT) {
            presenter.deleteDevice()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        presenter.onRequestPermissionResult(requestCode, permissions, grantResults, this)
    }
}