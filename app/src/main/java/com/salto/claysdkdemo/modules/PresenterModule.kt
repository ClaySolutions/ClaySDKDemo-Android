package com.salto.claysdkdemo.modules

import android.content.Context
import android.os.Handler
import com.myclay.claysdk.api.IClaySDK
import com.salto.claysdkdemo.application.ISharedPrefsUtil
import com.salto.claysdkdemo.login.oid.IOIDConfig
import com.salto.claysdkdemo.login.presenters.ILoginPresenter
import com.salto.claysdkdemo.login.presenters.LoginPresenter
import com.salto.claysdkdemo.main.presenters.IMainPresenter
import com.salto.claysdkdemo.main.presenters.MainPresenter
import com.salto.claysdkdemo.services.IDeviceService
import dagger.Module
import dagger.Provides
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationService

@Module
class PresenterModule {

    @Provides
    fun provideLoginPresenter(
        context: Context, sharedPrefs: ISharedPrefsUtil,
        authorizationService: AuthorizationService,
        authState: AuthState, oidConfig: IOIDConfig
    ): ILoginPresenter.Action {
        return LoginPresenter(context, sharedPrefs, authorizationService, authState, oidConfig)
    }

    @Provides
    fun provideMainPresenter(
        context: Context, sharedPrefs: ISharedPrefsUtil,
        authState: AuthState, oidConfig: IOIDConfig,
        deviceService: IDeviceService, claySDK: IClaySDK, handler: Handler
    ): IMainPresenter.Action {
        return MainPresenter(context, sharedPrefs, authState, oidConfig, deviceService, claySDK, handler)
    }
}