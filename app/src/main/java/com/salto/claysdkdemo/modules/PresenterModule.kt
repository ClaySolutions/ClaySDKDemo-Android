package com.salto.claysdkdemo.modules

import android.content.Context
import android.os.Handler
import com.myclay.claysdk.api.IClaySDK
import com.salto.claysdkdemo.access_code.presenters.AccessCodePresenter
import com.salto.claysdkdemo.access_code.presenters.IAccessCodePresenter
import com.salto.claysdkdemo.application.AppConfig
import com.salto.claysdkdemo.application.AppConfig.Dagger.API_KEY
import com.salto.claysdkdemo.application.ISharedPrefsUtil
import com.salto.claysdkdemo.login.oid.IOIDConfig
import com.salto.claysdkdemo.login.presenters.ILoginPresenter
import com.salto.claysdkdemo.login.presenters.LoginPresenter
import com.salto.claysdkdemo.guest_digital_key.presenters.GuestDigitalKeysListPresenter
import com.salto.claysdkdemo.guest_digital_key.presenters.IGuestDigitalKeysListPresenter
import com.salto.claysdkdemo.login.oid.OIDConfig
import com.salto.claysdkdemo.main.presenters.IMainPresenter
import com.salto.claysdkdemo.main.presenters.MainPresenter
import com.salto.claysdkdemo.services.IDeviceService
import com.salto.claysdkdemo.services.IPodService
import dagger.Module
import dagger.Provides
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationService
import javax.inject.Named

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
        deviceService: IDeviceService, @Named(API_KEY)apiKey: String,
        claySDK: IClaySDK, handler: Handler
    ): IMainPresenter.Action {
        return MainPresenter(context, sharedPrefs, authState, oidConfig, deviceService, apiKey, claySDK, handler)
    }

    @Provides
    fun provideDigitalGuestListPresenter(
        context: Context, sharedPrefs: ISharedPrefsUtil, handler: Handler): IGuestDigitalKeysListPresenter.Action {
        return GuestDigitalKeysListPresenter(context, sharedPrefs, handler)
    }

    @Provides
    fun provideAccessCodePresenter(
        context: Context, sharedPrefs: ISharedPrefsUtil,
        authorizationService: AuthorizationService,
        podService: IPodService,
        deviceService: IDeviceService,
        @Named(API_KEY) apiKey: String,
        oidConfig: OIDConfig,
        authState: AuthState
    ): IAccessCodePresenter.Action {
        return AccessCodePresenter(context, sharedPrefs, authorizationService, podService, deviceService, apiKey, oidConfig, authState)
    }
}