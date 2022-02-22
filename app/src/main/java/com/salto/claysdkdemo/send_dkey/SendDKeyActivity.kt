package com.salto.claysdkdemo.send_dkey

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.app.ActivityCompat
import com.myclay.claysdk.api.error.ClayException
import com.salto.claysdkdemo.R
import com.salto.claysdkdemo.application.AppConfig
import com.salto.claysdkdemo.base.SaltoActivity
import com.salto.claysdkdemo.enums.MKActivationState
import com.salto.claysdkdemo.login.LoginActivity
import com.salto.claysdkdemo.main.presenters.IMainPresenter
import com.salto.claysdkdemo.models.GuestDigitalKey
import com.salto.claysdkdemo.models.Key
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.progress_bar
import javax.inject.Inject

class SendDKeyActivity : SaltoActivity<IMainPresenter.View, IMainPresenter.Action>(),
    IMainPresenter.View {

    @Inject
    lateinit var handler: Handler

    private var gdKey: GuestDigitalKey? = null

    private var status: String = ""
        set(value) {
            field = value
            status_tv.visibility = if(value.isEmpty()) GONE else VISIBLE
            status_tv.text = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        (intent?.extras?.get("GDKEY") as GuestDigitalKey?)?.let {
            this.gdKey = it
        }
        open_button.setOnClickListener {
            openLock()
        }
        logout_button.visibility = GONE
        retry_button.visibility = GONE
        open_button.visibility = VISIBLE
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    private fun openLock() {
        showProgress()
        status = getString(R.string.looking_for_looks)
        open_button.visibility = GONE
        presenter.openLock(key = gdKey)
    }

    override fun bindView() {
        presenter.bindView(this)
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

    override fun onSuccessWithCancelledKey() = Unit

    private fun reactivateDevice() = Unit

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

    override fun onMKeyDecryptionFailed() = Unit

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

    override fun onActivationStateChanged(state: MKActivationState?) = Unit

    private fun onRegistrationError() = Unit

    private fun showProgress() {
        progress_bar.visibility = VISIBLE
    }

    private fun hideProgress() {
        progress_bar.visibility = GONE
    }

    private fun onDeviceActivated() = Unit

    private fun onMobileKeyDownload() = Unit

    private fun onDeviceRegistration() = Unit

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) = Unit

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        presenter.onRequestPermissionResult(requestCode, permissions, grantResults, this)
    }
}