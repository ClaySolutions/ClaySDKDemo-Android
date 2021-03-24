package com.salto.claysdkdemo.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.StringRes
import com.myclay.claysdk.api.error.ClayException
import com.salto.claysdkdemo.R
import com.salto.claysdkdemo.application.App
import com.salto.claysdkdemo.application.AppConfig
import com.salto.claysdkdemo.main.presenters.IMainPresenter
import javax.inject.Inject

class DemoWidgetProvider : AppWidgetProvider(), IMainPresenter.View {


    @Inject
    lateinit var presenter: IMainPresenter.Action

    @Inject
    lateinit var context: Context

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        context?.let { ctx ->

            inject(ctx)

            appWidgetIds?.let {
                for (widgetId in it) {
                    val widget = getRemoteViews(ctx)

                    widget.setOnClickPendingIntent(R.id.widget_view, getPendingSelfIntent(context, AppConfig.Widget.WIDGET_CLICK_ACTION, widgetId))
                    resetWidget(widget)

                    appWidgetManager?.updateAppWidget(widgetId, widget)
                }
            }
        }
    }

    private fun getRemoteViews(context: Context): RemoteViews {
        return RemoteViews(context.packageName, R.layout.clay_appwidget)
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)

        context?.let {
            updateWidget(it)
        }
    }

    private fun updateWidget(context: Context) {
        AppWidgetManager.getInstance(context)?.updateAppWidget(getAppWidgetIds(context), getRemoteViews(context))
    }

    private fun partiallyUpdateAppWidget(context: Context, remoteViews: RemoteViews) {
        AppWidgetManager.getInstance(context)?.partiallyUpdateAppWidget(getAppWidgetIds(context), remoteViews)
    }

    private fun getAppWidgetIds(context: Context): IntArray? {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        return appWidgetManager?.getAppWidgetIds(ComponentName(context, DemoWidgetProvider::class.java))
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        context?.let {
            inject(it)
            val hasPermissions = presenter.checkAndAskRequiredPermission(true)

            when {
                AppConfig.Widget.WIDGET_CLICK_ACTION == intent?.action && hasPermissions -> {
                    val widget = getRemoteViews(context)
                    widget.setViewVisibility(R.id.mkey_iv_container, View.GONE)
                    widget.setViewVisibility(R.id.progress_bar, View.VISIBLE)

                    showToastMessage(R.string.mobile_key_active_widget)

                    partiallyUpdateAppWidget(context, widget)
                    presenter.openLock(true)
                }

                AppConfig.Widget.WIDGET_CLICK_ACTION == intent?.action && !hasPermissions ->
                    showToastMessage(it.getString(R.string.permissions_denied_widget))
            }
        }
    }

    private fun getPendingSelfIntent(context: Context, action: String, widgetId: Int): PendingIntent {
        val intent = Intent(context, javaClass)
        intent.action = action
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetId)

        return PendingIntent.getBroadcast(context, 0, intent, 0)
    }

    private fun inject(context: Context) {
        (context.applicationContext as? App)?.appComponent?.inject(this)
        presenter.bindView(this)
    }

    override fun onMobileKeyNotFound() {
        showError(R.string.mkey_not_found)
    }

    private fun showError(@StringRes messageId: Int) {
        showError(context.getString(messageId))
    }

    private fun showError(message: String?) {
        val widget = getRemoteViews(context)
        widget.setViewVisibility(R.id.progress_bar, View.GONE)
        message?.let {
            showToastMessage(it)
        }
        resetWidget(widget)
        partiallyUpdateAppWidget(context, widget)
    }

    private fun resetWidget(widget: RemoteViews) {
        widget.setViewVisibility(R.id.mkey_iv_container, View.VISIBLE)
        partiallyUpdateAppWidget(context, widget)
    }

    override fun onBluetoothStatusChanged(enabled: Boolean) {
        if (!enabled) {
            showError(context.getString(R.string.required_to_mobile_key))
        }
    }

    override fun onPeripheralFound() {
        val widget = getRemoteViews(context)
        showToastMessage(context.getString(R.string.lock_found))
        partiallyUpdateAppWidget(context, widget)
    }

    private fun showToastMessage(@StringRes messageId: Int) {
        showToastMessage(context.getString(messageId))
    }

    private fun showToastMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onKeySuccessfullySent() {
        val widget = getRemoteViews(context)
        widget.setViewVisibility(R.id.progress_bar, View.GONE)
        resetWidget(widget)
        showToastMessage(R.string.mobile_key_received_by_lock)
        partiallyUpdateAppWidget(context, widget)
    }

    override fun onTimeOut() {
        showError(R.string.lock_not_found)
    }

    override fun onKeySendError(error: String, exception: ClayException) {
        val errorMessage = if (exception.errorCode == ClayException.ErrorCodes.COARSE_LOCATION_PERMISSION_DENIED_ERROR) {
            "$error ${context.getString(R.string.mkey_coarse_location_permission_widget_addition)}"
        } else {
            error
        }
        showError(errorMessage)
    }

    override fun onMKeyDecryptionFailed() {
        showResetMobileKeyError()
    }

    private fun showResetMobileKeyError() {
        showError(context.getString(R.string.mkey_widget_reset))
    }

    override fun onSuccessWithCancelledKey() {
        showResetMobileKeyError()
    }
}