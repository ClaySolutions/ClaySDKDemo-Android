package com.salto.claysdkdemo.widget

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.salto.claysdkdemo.R
import com.salto.claysdkdemo.base.SaltoActivity
import com.salto.claysdkdemo.main.presenters.IMainPresenter

class AppWidgetPermissionActivity : SaltoActivity<IMainPresenter.View, IMainPresenter.Action>(), IMainPresenter.View {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.widget_permission_activity)
        if (presenter.hasLocationPermissions(true)) {
            showWidget()
            return
        }

        showProminentLocationDialog()
    }

    override fun bindView() {
        presenter.bindView(this)
    }

    /**
     * Because of Google's privacy policy, usage of the location in background permission needs to be
     * notified to the user before the feature which uses it is enabled. This is this dialog purpose before
     * actually asking for the location in background permission (Required by the widget)
     */

    private fun showProminentLocationDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)

        alertDialogBuilder
            .setTitle(R.string.digital_key_widget)
            .setMessage(getText(R.string.prominent_location_disclosure))
            .setPositiveButton(getText(R.string.ok)) { _: DialogInterface, _: Int -> checkForPermissions() }

        alertDialogBuilder.create().show()
    }

    private fun checkForPermissions() {
        if (!presenter.checkAndAskRequiredPermission(true)) {
            return
        }
        showWidget()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        if (grantResults.firstOrNull() != PackageManager.PERMISSION_GRANTED) {
            return
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q && !permissions.contains(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), requestCode)
            return
        }
        showWidget()
    }

    private fun showWidget() {
        val mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }

    override fun requestMissingPermissions(permissions: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(this, permissions, requestCode)
    }
}